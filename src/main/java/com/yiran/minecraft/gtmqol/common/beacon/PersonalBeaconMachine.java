package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiblockWorldSavedData;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.yiran.minecraft.gtmqol.common.multiblock.CyclicMultiblockMachine;
import com.yiran.minecraft.gtmqol.common.ui.NumberText;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEHatchPartMachine;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ChunkPos;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.MAX;
import static com.gregtechceu.gtceu.api.GTValues.V;

/**
 * Gives the players bound in its binding hatches the effects of the cores in its sockets, wherever they are, in
 * cycles of {@link #CYCLE_TICKS}: at a cycle's start it pays the cycle's energy for every bound player who is online,
 * in FE from its wireless FE input hatches, and gives them the effects for a little longer than the cycle. Can't pay:
 * nothing is given, so the effects run out.
 */
public class PersonalBeaconMachine extends CyclicMultiblockMachine {

    public static final int CYCLE_TICKS = 15 * 20;
    /** Longer than a cycle, so the effects don't flicker between two. */
    public static final int EFFECT_TICKS = CYCLE_TICKS + 3 * 20;
    /** Vanilla keeps an effect's amplifier in a byte. */
    private static final int MAX_LEVEL = 256;

    public static final String KEY = PersonalBeacon.KEY;

    /** The colour of the beam, 0 while the beacon is not working: clients draw the beam from this alone. */
    @SaveField
    @SyncToClient
    private int beamColor;
    /** How far around the controller chunks are kept loaded, -1 for none. */
    @SaveField
    private int loadedRadius = -1;

    private int layers;
    private final List<PersonalBeaconCoreSocketMachine> sockets = new ArrayList<>();
    private final List<PersonalBeaconBindingHatchMachine> bindings = new ArrayList<>();
    private final List<WirelessFEHatchPartMachine> feHatches = new ArrayList<>();
    private boolean chunkLoader;
    /** Effects and their levels, from the sockets. */
    private final Map<Holder<MobEffect>, Integer> offered = new LinkedHashMap<>();
    /** FE the last cycle cost; for the window. */
    private long lastCost;

    public PersonalBeaconMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    protected String langKey() {
        return KEY;
    }

    @Override
    protected int cycleTicks() {
        return CYCLE_TICKS;
    }

    public int beamColor() {
        return beamColor;
    }

    /** Effects the beacon gives and the highest level of each. */
    public Map<Holder<MobEffect>, Integer> offered() {
        return offered;
    }

    // ---------- structure ----------

    @Override
    public void formStructure(String substructureName) {
        // A formed structure that had to be checked in full again has changed. GTCEu then only adds parts, so start
        // over: the parts are exactly those of the structure as it is now, and the running cycle is dropped.
        // layers is 0 after a world load, when the saved structure forms as it was.
        if (isFormed() && layers > 0 &&
                getPatternState(substructureName).getState() == PatternState.CheckState.VALID_UNCACHED) {
            reform();
            return;
        }
        super.formStructure(substructureName);
        if (!isFormed()) return;
        layers = PersonalBeaconPyramid.layers(getLevel(), getBlockPos());
        sockets.clear();
        bindings.clear();
        feHatches.clear();
        chunkLoader = false;
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof PersonalBeaconCoreSocketMachine socket) sockets.add(socket);
            else if (part instanceof PersonalBeaconBindingHatchMachine binding) bindings.add(binding);
            else if (part instanceof WirelessFEHatchPartMachine hatch) feHatches.add(hatch);
            else if (part instanceof PersonalBeaconChunkLoaderMachine) chunkLoader = true;
        }
        onCoresChanged();
        loadChunks(chunkLoader ? layers : -1);
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        cancelCycle();
        if (beamColor != 0) setBeam(0);
        sockets.clear();
        bindings.clear();
        feHatches.clear();
        chunkLoader = false;
        offered.clear();
        loadChunks(-1);
    }

    /**
     * Forgets the structure and checks it from scratch. GTCEu checks a formed structure again only when one of its
     * blocks changes, so a layer added below it is not seen without this.
     */
    private void reform() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        PatternState state = getDefaultPatternState();
        if (isFormed()) invalidateStructure();
        MultiblockWorldSavedData.getOrCreate(serverLevel).removeMapping(state);
        invalidateStructureCaches();
        state.setState(PatternState.CheckState.UNINITIALIZED);
        checkAndFormStructure();
    }

    @Override
    public void onMachineDestroyed() {
        if (beamColor != 0 && getLevel() != null) {
            // a working beacon that is broken goes out like one that stops
            beamColor = 0;
            getLevel().playSound(null, getBlockPos(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1, 1);
        }
        loadChunks(-1);
        super.onMachineDestroyed();
    }

    /** Counts the cores again: one level per core, Flight once. */
    void onCoresChanged() {
        offered.clear();
        for (PersonalBeaconCoreSocketMachine socket : sockets) {
            Holder<MobEffect> effect = socket.effect();
            if (effect == null) continue;
            int max = effect.value() == BeaconEffects.FLIGHT.get() ? 1 : MAX_LEVEL;
            offered.merge(effect, 1, (a, b) -> Math.min(a + b, max));
        }
        if (beamColor != 0) setBeam(mixColor(offered.keySet()));
    }

    /**
     * Shows the beam in {@code color}, 0 for none, with a beacon's sounds when it comes and goes. Sent to clients
     * every time, also unchanged: the beam once stayed away on clients (the user, 2026-11-14).
     */
    private void setBeam(int color) {
        if (getLevel() != null && (color != 0) != (beamColor != 0)) {
            getLevel().playSound(null, getBlockPos(),
                    color != 0 ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1, 1);
        }
        beamColor = color;
        syncDataHolder.markClientSyncFieldDirty("beamColor");
        markAsChanged();
    }

    @Override
    protected void onCycleTick() {
        // as vanilla's beacon
        if (getLevel() != null && getOffsetTimer() % 80 == 0) {
            getLevel().playSound(null, getBlockPos(), SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1, 1);
        }
    }

    @Override
    protected void onIdleTick() {
        if (beamColor != 0) setBeam(0);
    }

    private static int mixColor(Set<Holder<MobEffect>> effects) {
        if (effects.isEmpty()) return 0;
        int r = 0, g = 0, b = 0;
        for (Holder<MobEffect> effect : effects) {
            int color = effect.value().getColor();
            r += color >> 16 & 255;
            g += color >> 8 & 255;
            b += color & 255;
        }
        int n = effects.size();
        return 0xFF000000 | (r / n) << 16 | (g / n) << 8 | (b / n);
    }

    /** Keeps the chunks within {@code radius} blocks of the controller loaded; -1 releases them. */
    private void loadChunks(int radius) {
        if (!(getLevel() instanceof ServerLevel serverLevel) || radius == loadedRadius) return;
        if (loadedRadius >= 0) forEachChunk(loadedRadius, chunk -> PersonalBeacon.forceChunk(serverLevel, getBlockPos(),
                chunk, false));
        if (radius >= 0) forEachChunk(radius, chunk -> PersonalBeacon.forceChunk(serverLevel, getBlockPos(), chunk, true));
        loadedRadius = radius;
        markAsChanged();
    }

    private void forEachChunk(int radius, Consumer<ChunkPos> action) {
        int x = getBlockPos().getX();
        int z = getBlockPos().getZ();
        for (int cx = (x - radius) >> 4; cx <= (x + radius) >> 4; cx++) {
            for (int cz = (z - radius) >> 4; cz <= (z + radius) >> 4; cz++) {
                action.accept(new ChunkPos(cx, cz));
            }
        }
    }

    // ---------- logic (server) ----------

    /** EU/t of one effect for one player: Flight 1A EV, else the voltage of its level's tier, ×4 per level past MAX. */
    public static long costPerTick(Holder<MobEffect> effect, int level) {
        if (effect.value() == BeaconEffects.FLIGHT.get()) return V[EV];
        if (level <= MAX) return V[level];
        return LongMath.saturatedMultiply(V[MAX], LongMath.saturatedPow(4, level - MAX));
    }

    private record Grant(ServerPlayer player, Holder<MobEffect> effect, int level) {}

    @Override
    protected @Nullable String startCycle() {
        String problem = grant();
        if (problem == null) setBeam(mixColor(offered.keySet()));
        return problem;
    }

    /** Pays for a cycle and gives the effects; the lang key of the reason when it can't. */
    private @Nullable String grant() {
        if (offered.isEmpty()) return KEY + "status.no_cores";
        var server = getLevel() == null ? null : getLevel().getServer();
        if (server == null) return KEY + "status.no_cores";
        List<Grant> grants = new ArrayList<>();
        long euPerTick = 0;
        Set<UUID> served = new HashSet<>();
        for (PersonalBeaconBindingHatchMachine binding : bindings) {
            UUID id = binding.player();
            // two hatches bound to one player give and cost once
            if (id == null || !served.add(id)) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) continue;
            for (var entry : offered.entrySet()) {
                int level = binding.level(entry.getKey(), entry.getValue());
                if (level <= 0) continue;
                grants.add(new Grant(player, entry.getKey(), level));
                euPerTick = LongMath.saturatedAdd(euPerTick, costPerTick(entry.getKey(), level));
            }
        }
        double fe = (double) euPerTick * CYCLE_TICKS * FeCompat.ratio(false);
        if (fe > 0 && !pay(fe)) return KEY + "status.no_energy";
        lastCost = (long) Math.min(fe, Long.MAX_VALUE);
        for (Grant grant : grants) {
            grant.player().addEffect(new MobEffectInstance(grant.effect(), EFFECT_TICKS, grant.level() - 1,
                    true, false, true));
        }
        return null;
    }

    /** Takes {@code fe} from the FE hatches, all or nothing. */
    private boolean pay(double fe) {
        double available = 0;
        // hatches bound to one wireless network each see all of its FE: count it once
        Set<Object> networks = new HashSet<>();
        for (WirelessFEHatchPartMachine hatch : feHatches) {
            Object network = hatch.binding.getNetworkId();
            if (!networks.add(network == null ? hatch : network)) continue;
            available += hatch.extractFE(fe - available, true);
            if (available >= fe) break;
        }
        if (available < fe) return false;
        double left = fe;
        for (WirelessFEHatchPartMachine hatch : feHatches) {
            if (left <= 0) break;
            left -= hatch.extractFE(left, false);
        }
        return true;
    }

    @Override
    protected void finishCycle() {}

    // ---------- UI ----------

    private record Row(ResourceLocation effect, int level) {

        static final StreamCodec<RegistryFriendlyByteBuf, Row> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Row::effect,
                ByteBufCodecs.VAR_INT, Row::level,
                Row::new);
    }

    private List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        offered.forEach((effect, level) -> effect.unwrapKey()
                .ifPresent(key -> rows.add(new Row(key.location(), level))));
        return rows;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var rows = new GenericListSyncHandler.Builder<RegistryFriendlyByteBuf, Row>()
                .getter(() -> isRemote() ? List.of() : rows())
                .serializer(Row.STREAM_CODEC)
                .deserializer(Row.STREAM_CODEC)
                .build();
        var layersValue = new IntSyncValue(() -> isFormed() ? layers : 0);
        var playersValue = new IntSyncValue(() -> (int) bindings.stream()
                .map(PersonalBeaconBindingHatchMachine::player).filter(Objects::nonNull).distinct().count());
        var costValue = new LongSyncValue(() -> lastCost);
        var loadingValue = new IntSyncValue(() -> isFormed() && chunkLoader ? 1 : 0);
        syncManager.syncValue("rows", rows);
        syncManager.syncValue("layers", layersValue);
        syncManager.syncValue("players", playersValue);
        syncManager.syncValue("cost", costValue);
        syncManager.syncValue("loading", loadingValue);
        syncManager.registerSyncedAction("recheck", false, true, buf -> reform());

        var list = new ListWidget<>()
                .widthRel(1f)
                .height(60)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER);
        int count = BuiltInRegistries.MOB_EFFECT.size();
        for (int i = 0; i < count; i++) {
            int index = i;
            Supplier<@Nullable Row> row = () -> index < rows.getValue().size() ? rows.getValue().get(index) : null;
            list.child(new TextWidget<>(Text.dynamic(() -> {
                Row r = row.get();
                if (r == null) return Component.empty();
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(r.effect());
                Component name = effect == null ? Component.literal(r.effect().toString()) : effect.getDisplayName();
                return Component.translatable(KEY + "effect", name, r.level());
            }))
                    .height(12)
                    .setEnabledIf(widget -> row.get() != null));
        }

        // the power button is the column's last child: on the panel itself it lay over the first lines
        mainWidget.child(Flow.col()
                .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                .coverChildrenHeight()
                .childPadding(3)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .child(statusLine(syncManager))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(KEY + "size",
                        layersValue.getIntValue(), playersValue.getIntValue()))))
                .child(NumberText.line(format -> Component.translatable(KEY + "cost",
                        format.apply(costValue.getLongValue()))))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                        loadingValue.getIntValue() > 0 ? KEY + "chunks.on" : KEY + "chunks.off"))))
                .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                        rows.getValue().isEmpty() ? KEY + "effects.none" : KEY + "effects"))))
                .child(list)
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .child(powerButton(syncManager))
                        .child(new ButtonWidget<>()
                                .size(90, 18)
                                .overlay(Text.lang(KEY + "recheck"))
                                .tooltip(t -> t.addLine(Component.translatable(KEY + "recheck.tooltip")))
                                .onMousePressed((context, button) -> {
                                    syncManager.callSyncedAction("recheck");
                                    return true;
                                }))));
    }
}
