package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fishes with the rod in its controller, every {@link #CYCLE_TICKS}. While working it fills an internal buffer from
 * its energy hatches as fast as they allow, up to {@link #CYCLE_TICKS} ticks of their combined input. At the end of a
 * cycle the buffer pays for {@code m} times the configured number of rolls, {@code m} as large as it affords: each
 * roll of vanilla's fishing loot is multiplied by {@code m}. Can't afford one round: nothing happens and the energy
 * stays for the next cycle. Never loses power, and the rod is never damaged.
 *
 * <p>Like vanilla: Luck of the Sea is loot luck, Lure shortens the wait (here: makes a fish cheaper, by the share of
 * the average 350 tick wait it cuts). Without treasure mode the hook isn't in open water (fish and junk);
 * treasure mode is open water (fish, junk and treasure) for {@link #TREASURE_COST_MULTIPLIER} times the cost.</p>
 */
public class FishingPondMachine extends CyclicMultiblockMachine {

    public static final int CYCLE_TICKS = 5 * 20;
    public static final long EU_PER_FISH = 1000;
    public static final int TREASURE_COST_MULTIPLIER = 4;
    public static final int MAX_ROLLS = 64;
    /**
     * Vanilla's wait is uniform between 100 and 600 ticks.
     */
    private static final float AVERAGE_WAIT_TICKS = 350;
    /**
     * Lure III.
     */
    private static final float MAX_LURE_TICKS = 300;

    public static final String KEY = "gtmqol.fishing_pond.";

    @SaveField
    public final CustomItemStackHandler rod;
    @SaveField
    private boolean treasure;
    @SaveField
    private int rolls = 1;
    @SaveField
    private long energyBuffer;

    public FishingPondMachine(BlockEntityCreationInfo info) {
        super(info);
        this.rod = new CustomItemStackHandler(1);
        rod.setFilter(stack -> stack.canPerformAction(ToolActions.FISHING_ROD_CAST));
        rod.setOnContentsChanged(this::markAsChanged);
        attachPersistentTrait("fluidRendererTrait", new MultiblockFluidRendererTrait(this::waterOffsets));
    }

    /**
     * The cavity is 5 × 5, starting right behind the controller; water is drawn on its second layer from the top.
     */
    private Set<BlockPos> waterOffsets() {
        Direction front = getFrontFacing();
        Direction upwards = getUpwardsFacing();
        boolean flipped = isFlipped();
        Direction up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);
        Direction back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(front, upwards, flipped);
        BlockPos origin = BlockPos.ZERO.relative(up, 2);
        Set<BlockPos> offsets = new HashSet<>();
        for (int depth = 1; depth <= 5; depth++) {
            for (int side = -2; side <= 2; side++) {
                offsets.add(origin.relative(back, depth).relative(right, side));
            }
        }
        return offsets;
    }

    @Override
    public void modifyDrops(List<ItemStack> drops) {
        super.modifyDrops(drops);
        if (!rod.getStackInSlot(0).isEmpty()) drops.add(rod.getStackInSlot(0).copy());
    }

    @Override
    protected String langKey() {
        return KEY;
    }

    @Override
    protected int cycleTicks() {
        return CYCLE_TICKS;
    }

    // ---------- logic (server) ----------

    private List<NotifiableEnergyContainer> energyInputs() {
        return getTraitsFromParts(NotifiableEnergyContainer.class).stream()
                .filter(container -> container.getHandlerIO() == IO.IN)
                .toList();
    }

    /**
     * A cycle's worth of the hatches' combined input.
     */
    private static long bufferCapacity(List<NotifiableEnergyContainer> inputs) {
        long perTick = 0;
        for (NotifiableEnergyContainer container : inputs) {
            perTick = LongMath.saturatedAdd(perTick,
                    LongMath.saturatedMultiply(container.getInputVoltage(), container.getInputAmperage()));
        }
        return LongMath.saturatedMultiply(perTick, CYCLE_TICKS);
    }

    @Override
    protected @Nullable String startCycle() {
        return rod.getStackInSlot(0).isEmpty() ? KEY + "status.no_rod" : null;
    }

    @Override
    protected void onCycleTick() {
        List<NotifiableEnergyContainer> inputs = energyInputs();
        long capacity = bufferCapacity(inputs);
        for (NotifiableEnergyContainer container : inputs) {
            long room = capacity - energyBuffer;
            if (room <= 0) break;
            long take = Math.min(room, container.getEnergyStored());
            if (take > 0) energyBuffer -= container.changeEnergy(-take);
        }
    }

    /**
     * EU per fish with this rod, Lure included, rounded up.
     */
    private long costPerFish(ItemStack tool) {
        // each Lure level cuts 5 s, like vanilla
        float lureTicks = Math.min(EnchantmentHelper.getFishingSpeedBonus(tool) * 5 * 20, MAX_LURE_TICKS);
        long base = EU_PER_FISH * (treasure ? TREASURE_COST_MULTIPLIER : 1);
        return (long) Math.ceil(base * (AVERAGE_WAIT_TICKS - lureTicks) / AVERAGE_WAIT_TICKS);
    }

    @Override
    protected void finishCycle() {
        ItemStack tool = rod.getStackInSlot(0);
        if (tool.isEmpty()) return;
        ServerLevel level = (ServerLevel) getLevel();
        // not added to the level; a new hook is in open water
        FishingHook hook = new FishingHook(EntityType.FISHING_BOBBER, level);
        long perRound = costPerFish(tool) * rolls;
        long multiplier = energyBuffer / perRound;
        if (multiplier < 1) return;
        energyBuffer -= multiplier * perRound;

        LootTable table = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
        // the treasure pool requires the hook to be in open water; without a hook its condition fails
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(getBlockPos()))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, treasure ? hook : null)
                .withLuck(EnchantmentHelper.getFishingLuckBonus(tool))
                .create(LootContextParamSets.FISHING);
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : table.getRandomItems(params)) {
                output.add(stack, stack.getCount() * multiplier);
            }
        }
        markAsChanged();
    }

    private void setTreasure(boolean treasure) {
        this.treasure = treasure;
        markAsChanged();
    }

    private void setRolls(int rolls) {
        this.rolls = Mth.clamp(rolls, 1, MAX_ROLLS);
        markAsChanged();
    }

    // ---------- UI ----------

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var bufferValue = new LongSyncValue(() -> energyBuffer);
        var capacityValue = new LongSyncValue(() -> !isRemote() && isFormed() ? bufferCapacity(energyInputs()) : 0);
        var rollsValue = new IntSyncValue(() -> rolls, this::setRolls).allowC2S();
        var treasureValue = new BooleanSyncValue(() -> treasure, this::setTreasure).allowC2S();
        syncManager.syncValue("buffer", bufferValue);
        syncManager.syncValue("capacity", capacityValue);
        syncManager.syncValue("rolls", rollsValue);
        syncManager.syncValue("treasure", treasureValue);

        var config = syncManager.syncedPanel("fishing_pond_config", false,
                (manager, handler) -> configPanel(rollsValue));

        mainWidget.child(Flow.col()
                        .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                        .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT + 24)
                        .mainAxisAlignment(Alignment.MainAxis.CENTER)
                        .childPadding(4)
                        .child(statusLine(syncManager).horizontalCenter())
                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(KEY + "buffer",
                                FormattingUtil.formatNumbers(bufferValue.getLongValue()),
                                FormattingUtil.formatNumbers(capacityValue.getLongValue()))))
                                .horizontalCenter())
                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(KEY + "settings",
                                rollsValue.getIntValue(), Component.translatable(
                                        treasureValue.getBoolValue() ? KEY + "mode.treasure" : KEY + "mode.normal")))))
                        .horizontalCenter())
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .horizontalCenter()
                        .child(new ItemSlot()
                                .slot(new ModularSlot(rod, 0).singletonSlotGroup(0).accessibility(true, true))
                                .background(GTGuiTextures.SLOT)
                                .tooltipBuilder(t -> t.addLine(Component.translatable(KEY + "rod"))))
                        .child(storedButton(syncManager, MAX_ROLLS))
                        .child(new ButtonWidget<>()
                                .size(44, 18)
                                .overlay(Text.lang(KEY + "config"))
                                .onMousePressed((context, button) -> {
                                    togglePanel(config);
                                    return true;
                                }))
                        .child(new ToggleButton()
                                .value(treasureValue)
                                .overlay(false, new ItemDrawable(Items.NAUTILUS_SHELL).asIcon().size(16))
                                .overlay(true, new ItemDrawable(Items.NAUTILUS_SHELL).asIcon().size(16))
                                .background(GuiTextures.MC_BUTTON)
                                .background(true, GuiTextures.MC_BUTTON_PRESSED)
                                .tooltipDynamic(t -> t.addLine(Component.translatable(treasureValue.getBoolValue() ?
                                        KEY + "treasure.on" : KEY + "treasure.off")))
                                .tooltipAutoUpdate(true))
                        .child(powerButton(syncManager)));
    }

    private static ModularPanel<?> configPanel(IntSyncValue rolls) {
        return popup("fishing_pond_config", 160, 54)
                .child(Flow.column()
                        .padding(6)
                        .childPadding(4)
                        .child(Text.lang(KEY + "config.title").asWidget())
                        .child(counterRow(rolls, MAX_ROLLS, 8, KEY + "config.rolls")));
    }
}
