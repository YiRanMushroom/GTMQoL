package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH_INPUT;

/** The {@link GhostIngredientRecipeCapability} and its {@link GhostIngredientHatchPartMachine}. */
public final class GhostIngredients {

    /** Parts that provide marked items. The hatch also counts as an input bus, so any multiblock takes it. */
    public static final PartAbility GHOST_INGREDIENTS = new PartAbility("ghost_ingredients");

    public static MachineEntry<MachineDefinition> GHOST_INGREDIENT_HATCH;

    private GhostIngredients() {}

    /** Call during mod construction. */
    public static void init() {
        // the types register themselves when loaded; load them before anything is decoded
        ItemGhostIngredient.TYPE.id();
        FluidGhostIngredient.TYPE.id();
        GhostIngredientRecipeCapability.init();
        GHOST_INGREDIENT_HATCH = GTMQoLAddon.machine("ghost_ingredient_hatch", GhostIngredientHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                // the tiered hull model needs a tier
                .tier(LV)
                .abilities(GHOST_INGREDIENTS, PartAbility.IMPORT_ITEMS)
                .modelProperty(IS_FORMED, false)
                // gtceu's input bus textures; the String overload would look in our namespace
                .colorOverlayTieredHullModel(overlay(OVERLAY_ITEM_HATCH_INPUT), overlay("overlay_pipe"),
                        overlay("overlay_pipe_in_emissive"))
                .langValue("Ghost Ingredient Hatch")
                .register();

        var lang = GTMQoLAddon.registrate();
        String key = GhostIngredientHatchPartMachine.KEY;
        // gtceu shows <namespace>.machine.<id>.tooltip on its own
        lang.addRawLang(key + "tooltip", "Marks ingredients that tell the machine what to make. Nothing is consumed.");
        lang.addRawLang(key + "title", "Marked ingredients");
        lang.addRawLang(key + "hint.mark",
                "Left click: the held item. Right click: what it holds. Or drag from the recipe viewer.");
        lang.addRawLang(key + "hint.remove", "Click with an empty hand to remove");
        lang.addRawLang(GhostIngredientRecipeViewer.TOOLTIP_KEY, "Mark it in a Ghost Ingredient Hatch; not consumed");
    }

    private static ResourceLocation overlay(String name) {
        return GTCEu.id("block/overlay/machine/" + name);
    }

    public static void addRecipes(RecipeOutput provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("ghost_ingredient_hatch"))
                .inputItems(GTMachines.ITEM_IMPORT_BUS[LV].asStack())
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .circuitMeta(11)
                .outputItems(GHOST_INGREDIENT_HATCH.asStack())
                .duration(200)
                .save(provider);
    }
}
