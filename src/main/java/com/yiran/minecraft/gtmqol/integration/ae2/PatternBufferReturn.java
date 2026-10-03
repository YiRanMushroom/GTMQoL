package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IFilteredHandler;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import brachy.modularui.api.drawable.Text;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Lets a pattern buffer take the outputs of the multiblock it is part of straight into the ME network, used by
 * {@code mixin.MEPatternBufferReturnMixin}. It is an extra OUT handler list with the highest priority: while the
 * network accepts the outputs they never reach the output buses, so the crafting CPU gets them the moment the
 * recipe finishes. Whatever the network refuses (offline, full) is left for the other output parts.
 * <p>
 * Every output of the machine goes that way, not only those of recipes pushed from patterns.
 */
public final class PatternBufferReturn {

    public static final String TOGGLE_KEY = "gtmqol.gui.pattern_buffer.return";

    private PatternBufferReturn() {}

    /**
     * The handlers check the switches themselves, on every call: the list is added to the multiblock when it
     * forms and must stay the same object until it unforms.
     */
    public static RecipeHandlerList createHandlerList(MEPatternBufferPartMachine buffer, BooleanSupplier enabled) {
        return RecipeHandlerList.of(IO.OUT, -1, new ItemReturn(buffer, enabled), new FluidReturn(buffer, enabled));
    }

    public static MachineUIPanelBuilder addToggle(MachineUIPanelBuilder builder, BooleanSupplier enabled,
                                                  BooleanConsumer setEnabled) {
        var left = builder.leftConfigurators();
        return builder.leftConfigurators(left.andThen(f -> f.child(GTMuiWidgets.createToggleButton(enabled,
                setEnabled, GTGuiTextures.BUTTON_BATCH[0], GTGuiTextures.BUTTON_BATCH[1], TOGGLE_KEY)
                .overlay(Text.str("ME").asIcon().size(16)))));
    }

    private abstract static class Return<K> implements IRecipeHandler<K> {

        private final MEPatternBufferPartMachine buffer;
        private final BooleanSupplier enabled;

        Return(MEPatternBufferPartMachine buffer, BooleanSupplier enabled) {
            this.buffer = buffer;
            this.enabled = enabled;
        }

        /** The network to insert into, or null when outputs should not go there. */
        @Nullable
        MEStorage network() {
            if (!enabled.getAsBoolean() || !GTMQoLConfig.get().ae2.patternBufferReturn || !buffer.isOnline()) {
                return null;
            }
            var grid = buffer.getMainNode().getGrid();
            return grid == null ? null : grid.getStorageService().getInventory();
        }

        Actionable mode(boolean simulate) {
            return simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        }

        long insert(MEStorage network, AEKey key, long amount, boolean simulate) {
            return network.insert(key, amount, mode(simulate), buffer.getActionSource());
        }

        @Override
        public List<Object> getContents() {
            return Collections.emptyList();
        }

        @Override
        public double getTotalContentAmount() {
            return 0;
        }

        @Override
        public int getPriority() {
            return IFilteredHandler.HIGHEST;
        }
    }

    private static final class ItemReturn extends Return<SizedIngredient> {

        ItemReturn(MEPatternBufferPartMachine buffer, BooleanSupplier enabled) {
            super(buffer, enabled);
        }

        @Override
        public List<SizedIngredient> handleRecipeInner(IO io, @Nullable GTRecipe recipe, List<SizedIngredient> left,
                                                       boolean simulate) {
            if (io != IO.OUT) return left;
            MEStorage network = network();
            if (network == null) return left;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                boolean ranged = ingredient.ingredient().getCustomIngredient() instanceof IntProviderIngredient;
                if (!ranged && ingredient.ingredient().hasNoItems()) {
                    it.remove();
                    continue;
                }
                ItemStack stack;
                int amount;
                if (ranged && simulate) {
                    var provider = (IntProviderIngredient) ingredient.ingredient().getCustomIngredient();
                    stack = provider.getMaxSizeStack();
                    amount = provider.getMaxRoll();
                } else {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length == 0 || items[0].isEmpty()) {
                        it.remove();
                        continue;
                    }
                    stack = items[0];
                    amount = ingredient.count();
                }
                long remaining = amount - insert(network, AEItemKey.of(stack), amount, simulate);
                if (remaining > 0) it.set(new SizedIngredient(ingredient.ingredient(), (int) remaining));
                else it.remove();
            }
            return left;
        }

        @Override
        public RecipeCapability<SizedIngredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }
    }

    private static final class FluidReturn extends Return<SizedFluidIngredient> {

        FluidReturn(MEPatternBufferPartMachine buffer, BooleanSupplier enabled) {
            super(buffer, enabled);
        }

        @Override
        public List<SizedFluidIngredient> handleRecipeInner(IO io, @Nullable GTRecipe recipe,
                                                            List<SizedFluidIngredient> left, boolean simulate) {
            if (io != IO.OUT) return left;
            MEStorage network = network();
            if (network == null) return left;
            for (var it = left.listIterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.ingredient().hasNoFluids()) {
                    it.remove();
                    continue;
                }
                FluidStack[] fluids;
                if (ingredient.ingredient() instanceof IntProviderFluidIngredient provider && simulate) {
                    fluids = new FluidStack[] { provider.getMaxSizeStack() };
                } else {
                    fluids = ingredient.getFluids();
                }
                if (fluids.length == 0 || fluids[0].isEmpty()) {
                    it.remove();
                    continue;
                }
                long remaining = ingredient.amount() - insert(network, AEFluidKey.of(fluids[0]), ingredient.amount(),
                        simulate);
                if (remaining > 0) it.set(new SizedFluidIngredient(ingredient.ingredient(), (int) remaining));
                else it.remove();
            }
            return left;
        }

        @Override
        public RecipeCapability<SizedFluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }
    }
}
