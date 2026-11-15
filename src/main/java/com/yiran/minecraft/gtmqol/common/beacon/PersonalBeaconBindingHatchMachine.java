package com.yiran.minecraft.gtmqol.common.beacon;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.MouseData;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Binds its Personal Beacon to one player, who then gets the beacon's effects, each at the level set here: by
 * default the highest the beacon offers. Only that player changes the levels or unbinds.
 */
public class PersonalBeaconBindingHatchMachine extends MultiblockPartMachine implements IMuiMachine {

    @SaveField
    @SyncToClient
    private @Nullable UUID player;
    @SaveField
    @SyncToClient
    private String playerName = "";
    /** Levels the player set, by effect; an effect not in here gets the highest. */
    @SaveField
    private Map<ResourceLocation, Integer> levels = new HashMap<>();

    public PersonalBeaconBindingHatchMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    public @Nullable UUID player() {
        return player;
    }

    /** The level {@code effect} is given at, at most {@code max}; 0 is off. */
    public int level(MobEffect effect, int max) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        Integer set = id == null ? null : levels.get(id);
        return set == null ? max : Math.min(set, max);
    }

    /** Bound to another player. */
    private boolean isLockedFor(Player who) {
        return player != null && !player.equals(who.getUUID());
    }

    private void bind(Player who) {
        if (isLockedFor(who)) return;
        player = who.getUUID();
        playerName = who.getGameProfile().getName();
        markAsChanged();
    }

    private void unbind(Player who) {
        if (isLockedFor(who)) return;
        player = null;
        playerName = "";
        levels.clear();
        markAsChanged();
    }

    private void setLevel(Player who, ResourceLocation effect, int level, int max) {
        if (isLockedFor(who)) return;
        // the highest is the default, so a beacon that gets more cores raises it
        if (level >= max) levels.remove(effect);
        else levels.put(effect, Math.max(level, 0));
        markAsChanged();
    }

    private @Nullable PersonalBeaconMachine beacon() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof PersonalBeaconMachine beacon && beacon.isFormed()) return beacon;
        }
        return null;
    }

    /** An effect the beacon offers: its level here and the highest. */
    private record Row(ResourceLocation effect, int level, int max) {

        static Row read(FriendlyByteBuf buf) {
            return new Row(buf.readResourceLocation(), buf.readVarInt(), buf.readVarInt());
        }

        static void write(FriendlyByteBuf buf, Row row) {
            buf.writeResourceLocation(row.effect());
            buf.writeVarInt(row.level());
            buf.writeVarInt(row.max());
        }
    }

    private List<Row> rows() {
        PersonalBeaconMachine beacon = beacon();
        if (beacon == null) return List.of();
        List<Row> rows = new ArrayList<>();
        beacon.offered().forEach((effect, max) -> {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (id != null) rows.add(new Row(id, level(effect, max), max));
        });
        return rows;
    }

    // ---------- UI ----------

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        Player viewer = guiData.getPlayer();
        var name = new StringSyncValue(() -> playerName);
        var rows = new GenericListSyncHandler.Builder<Row>()
                .getter(() -> isRemote() ? List.of() : rows())
                .serializer(Row::write)
                .deserializer(Row::read)
                .build();
        syncManager.syncValue("player", name);
        syncManager.syncValue("rows", rows);
        syncManager.registerSyncedAction("bind", false, true, buf -> bind(viewer));
        syncManager.registerSyncedAction("unbind", false, true, buf -> unbind(viewer));
        syncManager.registerSyncedAction("level", false, true, buf -> {
            ResourceLocation effect = buf.readResourceLocation();
            int level = buf.readVarInt();
            for (Row row : rows()) {
                if (row.effect().equals(effect)) setLevel(viewer, effect, level, row.max());
            }
        });

        var list = new ListWidget<>()
                .widthRel(1f)
                .height(96)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER);
        // as many rows as there are effects: a beacon can't offer more
        int count = BuiltInRegistries.MOB_EFFECT.size();
        for (int i = 0; i < count; i++) {
            int index = i;
            Supplier<@Nullable Row> row = () -> index < rows.getValue().size() ? rows.getValue().get(index) : null;
            list.child(Flow.row()
                    .height(18)
                    .childPadding(4)
                    .coverChildrenWidth()
                    .setEnabledIf(widget -> row.get() != null)
                    .child(stepButton(syncManager, row, false))
                    .child(new TextWidget<>(Text.dynamic(() -> {
                        Row r = row.get();
                        if (r == null) return Component.empty();
                        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(r.effect());
                        Component effectName = effect == null ? Component.literal(r.effect().toString()) :
                                effect.getDisplayName();
                        return Component.translatable(PersonalBeacon.KEY + "binding.row", effectName, r.level(),
                                r.max());
                    }))
                            .width(120)
                            .verticalCenter())
                    .child(stepButton(syncManager, row, true)));
        }

        mainWidget.child(Flow.col()
                .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                .coverChildrenHeight()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .child(new TextWidget<>(Text.dynamic(() -> name.getStringValue().isEmpty() ?
                        Component.translatable(PersonalBeacon.KEY + "binding.unbound") :
                        Component.translatable(PersonalBeacon.KEY + "binding.bound", name.getStringValue()))))
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(4)
                        .child(new ButtonWidget<>()
                                .size(60, 18)
                                .overlay(Text.lang(PersonalBeacon.KEY + "binding.bind"))
                                .tooltip(t -> t.addLine(Component.translatable(PersonalBeacon.KEY +
                                        "binding.bind.tooltip")))
                                .onMousePressed((context, button) -> {
                                    syncManager.callSyncedAction("bind");
                                    return true;
                                }))
                        .child(new ButtonWidget<>()
                                .size(60, 18)
                                .overlay(Text.lang(PersonalBeacon.KEY + "binding.unbind"))
                                .onMousePressed((context, button) -> {
                                    syncManager.callSyncedAction("unbind");
                                    return true;
                                })))
                .child(new TextWidget<>(Text.dynamic(() -> rows.getValue().isEmpty() ?
                        Component.translatable(PersonalBeacon.KEY + "binding.no_effects")
                                .withStyle(ChatFormatting.GRAY) :
                        Component.translatable(PersonalBeacon.KEY + "binding.effects"))))
                .child(list));
    }

    /** -1 or +1; Shift: off or the highest. */
    private static ButtonWidget<?> stepButton(PanelSyncManager syncManager, Supplier<@Nullable Row> row, boolean up) {
        return new ButtonWidget<>()
                .size(14, 14)
                .verticalCenter()
                .overlay(Text.str(up ? "+" : "-"))
                .tooltip(t -> t.addLine(Component.translatable(PersonalBeacon.KEY +
                        (up ? "binding.up" : "binding.down"))))
                .onMousePressed((context, button) -> {
                    Row r = row.get();
                    if (r == null) return true;
                    boolean shift = MouseData.create(button).shift();
                    int level = up ? (shift ? r.max() : r.level() + 1) : (shift ? 0 : r.level() - 1);
                    level = Mth.clamp(level, 0, r.max());
                    if (level != r.level()) {
                        int send = level;
                        syncManager.callSyncedAction("level", buf -> {
                            buf.writeResourceLocation(r.effect());
                            buf.writeVarInt(send);
                        });
                    }
                    return true;
                });
    }
}
