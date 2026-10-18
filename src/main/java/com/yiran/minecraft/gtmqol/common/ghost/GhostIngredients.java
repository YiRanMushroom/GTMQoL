package com.yiran.minecraft.gtmqol.common.ghost;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH_INPUT;

/** The {@link GhostIngredientRecipeCapability} and its {@link GhostIngredientHatchPartMachine}. */
public final class GhostIngredients {

    /** Parts that provide marked items. The hatch also counts as an input bus, so any multiblock takes it. */
    public static final PartAbility GHOST_INGREDIENTS = new PartAbility("ghost_ingredients");

    public static MachineDefinition GHOST_INGREDIENT_HATCH;

    private GhostIngredients() {}

    /** Call during mod construction; the hatch comes in {@link #registerMachines}. */
    public static void init() {
        // the types register themselves when loaded; load them before anything is decoded
        ItemGhostIngredient.TYPE.id();
        FluidGhostIngredient.TYPE.id();
        GhostIngredientRecipeCapability.init();

        var lang = GTMQoLAddon.registrate();
        String key = GhostIngredientHatchPartMachine.KEY;
        // gtceu shows <namespace>.machine.<id>.tooltip on its own
        lang.addRawLang(key + "tooltip", "Marks ingredients that tell the machine what to make. Nothing is consumed.");
        lang.addRawLang(key + "tooltip.pattern_buffer", "With an ME Pattern Buffer, turn on its Shared Hatches.");
        lang.addRawLang(key + "title", "Marked ingredients");
        lang.addRawLang(key + "hint.mark",
                "Left click: the held item. Right click: what it holds. Or drag from the recipe viewer.");
        lang.addRawLang(key + "hint.remove", "Click with an empty hand to remove");
        lang.addRawLang(GhostIngredientRecipeViewer.TOOLTIP_KEY, "Mark it in a Ghost Ingredient Hatch; not consumed");
    }

    /** From gtceu's machine registry event. */
    public static void registerMachines() {
        GHOST_INGREDIENT_HATCH = GTMQoLAddon.machine("ghost_ingredient_hatch", GhostIngredientHatchPartMachine::new)
                .rotationState(RotationState.ALL)
                // the tiered hull model needs a tier
                .tier(LV)
                .abilities(GHOST_INGREDIENTS, PartAbility.IMPORT_ITEMS)
                .modelProperty(IS_FORMED, false)
                // gtceu's configurable maintenance hatch texture (a gear); the String overload would look in our
                // namespace
                .colorOverlayTieredHullModel(overlay("overlay_maintenance_configurable"))
                .langValue("Ghost Ingredient Hatch")
                // the main line is gtceu's own <namespace>.machine.<id>.tooltip
                .tooltips(Component.translatable(GhostIngredientHatchPartMachine.KEY + "tooltip.pattern_buffer"))
                .register();
    }

    private static ResourceLocation overlay(String name) {
        return GTCEu.id("block/overlay/machine/" + name);
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("ghost_ingredient_hatch"))
                .inputItems(GTMachines.ITEM_IMPORT_BUS[LV].asStack())
                .inputItems(CustomTags.LV_CIRCUITS, 2)
                .circuitMeta(11)
                .outputItems(GHOST_INGREDIENT_HATCH.asStack())
                .duration(200)
                .save(provider);
    }
}
