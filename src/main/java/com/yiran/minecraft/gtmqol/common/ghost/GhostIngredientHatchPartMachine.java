package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;

/**
 * A list of {@link GhostIngredient}s ({@link GhostIngredientHandler}), which tell the machine what to make; see
 * {@link GhostIngredientRecipeCapability}. Nothing is ever consumed. UI: {@link GhostIngredientListWidget}.
 */
public class GhostIngredientHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {

    public static final String KEY = "gtmqol.machine.ghost_ingredient_hatch.";
    private static final int WIDTH = 160;
    private static final int VISIBLE_ROWS = 6;

    private final GhostIngredientHandler handler;

    public GhostIngredientHatchPartMachine(BlockEntityCreationInfo info) {
        super(info);
        this.handler = attachTrait(new GhostIngredientHandler());
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        GhostIngredientList marks = handler.marks;
        var list = new GenericListSyncHandler<RegistryFriendlyByteBuf, GhostIngredient<?>>(marks::view, null,
                GhostIngredient.STREAM_CODEC, GhostIngredient.STREAM_CODEC, null, null);
        syncManager.syncValue("ghost_marks", list);

        syncManager.registerServerSyncedAction(GhostIngredientListWidget.CLICK, packet -> {
            int index = packet.readVarInt();
            int button = packet.readVarInt();
            ItemStack held = syncManager.getPlayer().containerMenu.getCarried();
            if (held.isEmpty()) {
                marks.remove(index);
                return;
            }
            GhostIngredient<?> ghost = GhostIngredient.fromHeld(held, button == 1);
            if (ghost != null) marks.set(index, ghost);
        });
        syncManager.registerServerSyncedAction(GhostIngredientListWidget.SET, packet -> {
            int index = packet.readVarInt();
            marks.set(index, GhostIngredient.STREAM_CODEC.decode(packet));
        });

        mainWidget.child(Flow.column()
                .size(WIDTH + 8, VISIBLE_ROWS * GhostIngredientListWidget.ROW + 22)
                .padding(4)
                .childPadding(4)
                .child(Text.lang(KEY + "title").asWidget())
                .child(new ListWidget<>()
                        .size(WIDTH, VISIBLE_ROWS * GhostIngredientListWidget.ROW)
                        .child(new GhostIngredientListWidget(list::getValue, syncManager, WIDTH - 8))));
    }
}
