package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Survival auto-build: places the planned structure from the clicking player's inventory, {@link #BLOCKS_PER_BATCH}
 * blocks at a time on the server thread. A click places one batch; shift-click toggles a job that places one batch
 * every {@link #INTERVAL} ticks until it is done or stuck.
 * <p>
 * Only air and replaceable blocks are built into; anything else is left alone. Every placement fires
 * {@code EntityPlaceEvent} so claim and protection mods can refuse it. Creative players use no items.
 */
public final class AutoBuilder {

    public static final int BLOCKS_PER_BATCH = 64;
    public static final int INTERVAL = 5;
    private static final double MAX_DISTANCE_SQR = 64 * 64;
    private static final int MISSING_SHOWN = 5;

    private static final String KEY = "gtmqol.auto_build.";
    public static final String BUTTON_KEY = KEY + "button";
    public static final String TOOLTIP_KEY = KEY + "tooltip.";
    private static final String PROGRESS_KEY = KEY + "progress";
    private static final String DONE_KEY = KEY + "done";
    private static final String FORMED_KEY = KEY + "formed";
    private static final String ALREADY_FORMED_KEY = KEY + "already_formed";
    private static final String STARTED_KEY = KEY + "started";
    private static final String STOPPED_KEY = KEY + "stopped";
    private static final String STOPPED_AWAY_KEY = KEY + "stopped_away";
    private static final String STOPPED_CONTROLLER_KEY = KEY + "stopped_controller";
    private static final String STUCK_KEY = KEY + "stuck";
    private static final String MISSING_KEY = KEY + "missing";
    private static final String BLOCKED_KEY = KEY + "blocked";
    private static final String FAILED_KEY = KEY + "failed";

    private record JobKey(ResourceKey<Level> dimension, BlockPos pos) {}

    private static final class Job {

        final UUID player;
        final MultiblockControllerMachine controller;
        final List<StructurePlanner.Entry> plan;
        int nextTick;

        Job(UUID player, MultiblockControllerMachine controller, List<StructurePlanner.Entry> plan) {
            this.player = player;
            this.controller = controller;
            this.plan = plan;
        }
    }

    private record Batch(int placed, int remaining, int blocked, Object2IntMap<Item> missing) {}

    private static final Map<JobKey, Job> JOBS = new HashMap<>();

    private AutoBuilder() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(AutoBuilder::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(AutoBuilder::onServerStopped);

        var registrate = GTMQoLAddon.registrate();
        registrate.addRawLang(BUTTON_KEY, "Auto build");
        registrate.addRawLang(TOOLTIP_KEY + "0", "Left click: place blocks from your inventory");
        registrate.addRawLang(TOOLTIP_KEY + "1", "Shift + left click: keep building until done, again to stop");
        registrate.addRawLang(TOOLTIP_KEY + "2", "Hatches, buses and other parts are not placed");
        registrate.addRawLang(TOOLTIP_KEY + "3", "Right click: settings");
        registrate.addRawLang(AutoBuildPanel.TITLE_KEY, "Auto build settings");
        registrate.addRawLang(AutoBuildPanel.LAYER_KEY, "Layer %s: %s");
        registrate.addRawLang(AutoBuildPanel.SIZE_KEY, "Size %s: %s");
        registrate.addRawLang(AutoBuildPanel.BLOCKS_KEY, "Block %s");
        registrate.addRawLang(AutoBuildPanel.RESET_KEY, "Reset");
        registrate.addRawLang(AutoBuildPanel.NOTHING_KEY, "Nothing to configure");
        registrate.addRawLang(PROGRESS_KEY, "Auto build: placed %s, %s left");
        registrate.addRawLang(DONE_KEY, "Auto build: every block is placed. Add the hatches to form the structure.");
        registrate.addRawLang(FORMED_KEY, "Auto build: the structure is formed");
        registrate.addRawLang(ALREADY_FORMED_KEY, "The structure is already formed");
        registrate.addRawLang(STARTED_KEY, "Auto build started");
        registrate.addRawLang(STOPPED_KEY, "Auto build stopped");
        registrate.addRawLang(STOPPED_AWAY_KEY, "Auto build stopped: you left the multiblock");
        registrate.addRawLang(STOPPED_CONTROLLER_KEY, "Auto build stopped: the controller is gone");
        registrate.addRawLang(STUCK_KEY, "Auto build: nothing could be placed, %s left");
        registrate.addRawLang(MISSING_KEY, "Missing: %s");
        registrate.addRawLang(BLOCKED_KEY, "%s positions are blocked, protected or not loaded");
        registrate.addRawLang(FAILED_KEY, "Auto build: cannot lay out this structure (%s)");
    }

    /** Server side of the button. */
    public static void onPressed(ServerPlayer player, MultiblockControllerMachine controller, boolean shift,
                                 BuildSettings settings) {
        if (!player.mayBuild() || !(controller.getLevel() instanceof ServerLevel level)) return;
        JobKey key = new JobKey(level.dimension(), controller.getBlockPos());
        if (shift && JOBS.remove(key) != null) {
            player.displayClientMessage(Component.translatable(STOPPED_KEY), true);
            return;
        }
        if (controller.isFormed()) {
            player.displayClientMessage(Component.translatable(ALREADY_FORMED_KEY), true);
            return;
        }
        List<StructurePlanner.Entry> plan;
        try {
            plan = StructurePlanner.plan(controller, settings);
        } catch (RuntimeException e) {
            GTMQoL.LOGGER.warn("Auto-build: cannot plan {} at {}", controller.getDefinition().getId(),
                    controller.getBlockPos(), e);
            player.displayClientMessage(Component.translatable(FAILED_KEY, String.valueOf(e.getMessage())), false);
            return;
        }
        if (shift) {
            JOBS.put(key, new Job(player.getUUID(), controller, plan));
            player.displayClientMessage(Component.translatable(STARTED_KEY), true);
            return;
        }
        afterBatch(player, controller, placeBatch(player, level, plan));
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        JOBS.clear();
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || JOBS.isEmpty()) return;
        MinecraftServer server = event.getServer();
        var iterator = JOBS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Job job = entry.getValue();
            if (server.getTickCount() < job.nextTick) continue;
            job.nextTick = server.getTickCount() + INTERVAL;
            if (!runJob(server, entry.getKey(), job)) iterator.remove();
        }
    }

    /** @return whether the job keeps running */
    private static boolean runJob(MinecraftServer server, JobKey key, Job job) {
        ServerPlayer player = server.getPlayerList().getPlayer(job.player);
        if (player == null) return false;
        ServerLevel level = server.getLevel(key.dimension());
        if (level == null || player.level() != level ||
                player.blockPosition().distSqr(key.pos()) > MAX_DISTANCE_SQR) {
            player.displayClientMessage(Component.translatable(STOPPED_AWAY_KEY), false);
            return false;
        }
        if (!level.isLoaded(key.pos()) || MetaMachine.getMachine(level, key.pos()) != job.controller) {
            player.displayClientMessage(Component.translatable(STOPPED_CONTROLLER_KEY), false);
            return false;
        }
        if (job.controller.isFormed()) {
            player.displayClientMessage(Component.translatable(FORMED_KEY), false);
            return false;
        }
        return afterBatch(player, job.controller, placeBatch(player, level, job.plan));
    }

    /** Reports a batch. @return whether there is more to do and the batch made progress */
    private static boolean afterBatch(ServerPlayer player, MultiblockControllerMachine controller, Batch batch) {
        if (batch.remaining() == 0) {
            // only now, a structure check per batch would be wasted on a half-built structure
            controller.getDefaultPatternState().getCache().clear();
            controller.checkAndFormStructure();
            player.displayClientMessage(Component.translatable(controller.isFormed() ? FORMED_KEY : DONE_KEY), false);
            return false;
        }
        if (batch.placed() > 0) {
            player.displayClientMessage(Component.translatable(PROGRESS_KEY, batch.placed(), batch.remaining()), true);
            return true;
        }
        player.displayClientMessage(Component.translatable(STUCK_KEY, batch.remaining()), false);
        if (!batch.missing().isEmpty()) {
            player.displayClientMessage(Component.translatable(MISSING_KEY, describe(batch.missing())), false);
        }
        if (batch.blocked() > 0) {
            player.displayClientMessage(Component.translatable(BLOCKED_KEY, batch.blocked()), false);
        }
        return false;
    }

    private static Batch placeBatch(ServerPlayer player, ServerLevel level, List<StructurePlanner.Entry> plan) {
        long start = System.nanoTime();
        boolean creative = player.getAbilities().instabuild;
        int placed = 0;
        int remaining = 0;
        int blocked = 0;
        Object2IntMap<Item> missing = new Object2IntLinkedOpenHashMap<>();
        // the whole plan is scanned every batch, so blocks broken in the meantime are rebuilt
        for (StructurePlanner.Entry entry : plan) {
            BlockPos pos = entry.pos();
            BlockState target = entry.state();
            if (!level.isLoaded(pos)) {
                remaining++;
                blocked++;
                continue;
            }
            BlockState current = level.getBlockState(pos);
            // Anything solid counts as done: a hatch the player put there, or a wrong block, which is left alone
            // (the structure check reports it).
            if (current.is(target.getBlock()) || !current.canBeReplaced()) continue;
            remaining++;
            if (placed >= BLOCKS_PER_BATCH) continue;
            Item item = target.getBlock().asItem();
            if (item == Items.AIR || !level.mayInteract(player, pos)) {
                blocked++;
                continue;
            }
            int slot = creative ? -1 : findItem(player.getInventory(), item);
            if (!creative && slot < 0) {
                missing.mergeInt(item, 1, Integer::sum);
                continue;
            }
            if (place(player, level, pos, target, slot)) {
                placed++;
                remaining--;
            } else {
                blocked++;
            }
        }
        GTMQoL.LOGGER.info("Auto-build: placed {} of {} planned blocks in {} us", placed, plan.size(),
                (System.nanoTime() - start) / 1000);
        return new Batch(placed, remaining, blocked, missing);
    }

    private static boolean place(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, int slot) {
        // same order as a normal block placement: place, fire the event, roll back if cancelled
        BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos);
        if (!level.setBlock(pos, state, Block.UPDATE_ALL)) return false;
        if (ForgeEventFactory.onBlockPlace(player, snapshot, Direction.UP)) {
            snapshot.restore(true, false);
            return false;
        }
        ItemStack stack = slot < 0 ? new ItemStack(state.getBlock()) : player.getInventory().getItem(slot);
        state.getBlock().setPlacedBy(level, pos, state, player, stack);
        if (slot >= 0) stack.shrink(1);
        return true;
    }

    private static int findItem(Inventory inventory, Item item) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) return i;
        }
        return -1;
    }

    private static Component describe(Object2IntMap<Item> missing) {
        MutableComponent text = Component.empty();
        int shown = 0;
        for (var entry : missing.object2IntEntrySet()) {
            if (shown == MISSING_SHOWN) {
                text.append(", ...");
                break;
            }
            if (shown > 0) text.append(", ");
            text.append(entry.getIntValue() + "x ").append(entry.getKey().getDescription());
            shown++;
        }
        return text;
    }
}
