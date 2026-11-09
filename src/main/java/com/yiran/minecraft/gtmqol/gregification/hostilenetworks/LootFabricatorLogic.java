package com.yiran.minecraft.gtmqol.gregification.hostilenetworks;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredient;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.ItemGhostIngredient;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import dev.shadowsoffire.hostilenetworks.Hostile;
import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.yiran.minecraft.gtmqol.gregification.hostilenetworks.HostileNetworksGregification.*;

/**
 * Recipes of the loot fabricator, as HNN's: 60 ticks at its fabricator cost, one prediction into one of its model's
 * fabricator drops. A ghost mark of the drop picks it, where HNN's fabricator has a selection screen; a model with
 * one drop needs no mark.
 */
// Untested: a prediction becomes the marked drop (a model with one drop needs no mark), EMI lists every model's drops
// and the simulation chamber's recipes, and both machines craft from the hull, field generators / sensor and emitter
enum LootFabricatorLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    /** HNN's fabricator runtime. */
    static final int DURATION = 60;

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        List<ItemStack> marked = marks(holder);
        for (List<ItemStack> items : inventories(holder)) {
            for (ItemStack stack : items) {
                if (!stack.is(Hostile.Items.PREDICTION)) continue;
                DynamicHolder<DataModel> model = DataModelItem.getStoredModel(stack);
                if (!model.isBound()) continue;
                List<ItemStack> drops = model.get().fabDrops();
                if (drops.size() == 1) return recipe(model.get(), drops.getFirst(), false);
                for (ItemStack drop : drops) {
                    for (ItemStack mark : marked) {
                        if (ItemStack.isSameItemSameComponents(drop, mark)) return recipe(model.get(), drop, true);
                    }
                }
            }
        }
        return null;
    }

    private static List<ItemStack> marks(IRecipeCapabilityHolder holder) {
        List<ItemStack> marks = new ArrayList<>();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : handlerList.getCapability(GhostIngredientRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof GhostIngredient<?> ghost && ghost.value() instanceof ItemStack item) {
                        marks.add(item);
                    }
                }
            }
        }
        return marks;
    }

    private static GTRecipe recipe(DataModel model, ItemStack drop, boolean marked) {
        String id = path(DataModelRegistry.INSTANCE.getKey(model)) + "/" + model.fabDrops().indexOf(drop);
        GTRecipeBuilder builder = recipeType(LOOT_FABRICATOR).recipeBuilder(GTMQoL.id(id))
                .inputItems(predictionIngredient(model))
                .outputItems(drop.copy())
                .EUt(euPerTick(HostileConfig.fabPowerCost))
                .duration(DURATION);
        if (marked) builder.input(GhostIngredientRecipeCapability.CAP, new ItemGhostIngredient(drop.copyWithCount(1)));
        return builder.build();
    }

    private static Ingredient predictionIngredient(DataModel model) {
        return DataComponentIngredient.of(false,
                DataComponentPredicate.builder()
                        .expect(Hostile.Components.DATA_MODEL, DataModelRegistry.INSTANCE.holder(model)).build(),
                Hostile.Items.PREDICTION.value());
    }

    @Override
    public void buildRepresentativeRecipes() {
        GTRecipeType type = recipeType(LOOT_FABRICATOR);
        for (DataModel model : DataModelRegistry.INSTANCE.getValues()) {
            List<ItemStack> drops = model.fabDrops();
            for (ItemStack drop : drops) addRepresentative(type, recipe(model, drop, drops.size() > 1));
        }
    }
}
