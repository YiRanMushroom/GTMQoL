package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The marks of a {@link GhostIngredientHatchPartMachine} ({@link GhostIngredientList}). Provides them to recipes as
 * {@link GhostIngredientRecipeCapability} inputs and never consumes them.
 */
public class GhostIngredientHandler extends NotifiableRecipeHandlerTrait<GhostIngredient<?>> {

    @SaveField
    public final GhostIngredientList marks = new GhostIngredientList();

    /** Size of {@link #buildList}. */
    public static final int LIST_WIDTH = 168;
    public static final int LIST_HEIGHT = 6 * GhostIngredientListWidget.ROW + 22;

    public GhostIngredientHandler() {
        // markAsChanged: the block entity is only saved again when marked as changed
        marks.setOnChanged(() -> {
            markAsChanged();
            notifyListeners();
        });
    }

    @Override
    public @NotNull List<GhostIngredient<?>> handleRecipeInner(IO io, @Nullable GTRecipe recipe,
                                                                  List<GhostIngredient<?>> left,
                                                                  boolean simulate) {
        if (io != IO.IN) return left;
        left.removeIf(marks::contains);
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return new ArrayList<>(new LinkedHashSet<>(marks.view()));
    }

    /**
     * The marks to edit, for a machine's UI: click with an item (or what it holds) to mark it, drag from the recipe
     * viewer, or click empty-handed to remove.
     */
    public Flow buildList(PanelSyncManager syncManager) {
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

        int width = LIST_WIDTH - 8;
        return Flow.column()
                .size(LIST_WIDTH, LIST_HEIGHT)
                .padding(4)
                .childPadding(4)
                .child(Text.lang(GhostIngredientHatchPartMachine.KEY + "title").asWidget())
                .child(new ListWidget<>()
                        .size(width, LIST_HEIGHT - 22)
                        .child(new GhostIngredientListWidget(list::getValue, syncManager, width - 8)));
    }

    @Override
    public double getTotalContentAmount() {
        return marks.size();
    }

    @Override
    public RecipeCapability<GhostIngredient<?>> getCapability() {
        return GhostIngredientRecipeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }
}
