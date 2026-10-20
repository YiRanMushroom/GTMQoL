package com.yiran.minecraft.gtmqol.common.probable;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.RecipeOutput;

import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Naquadria;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Polybenzimidazole;

/**
 * The Probable Certainty Device and the Probable Improbability Device ({@link ProbableDevicePartMachine}). Every
 * multiblock whose structure uses gtceu's {@code autoAbilities} for its recipe types takes them
 * ({@code PredicatesProbableDeviceMixin}).
 */
public final class ProbableDevices {

    public static final PartAbility PROBABLE_DEVICE = new PartAbility("probable_device");

    public static @Nullable MachineEntry<MachineDefinition> CERTAINTY;
    public static @Nullable MachineEntry<MachineDefinition> IMPROBABILITY;

    private ProbableDevices() {}

    public static boolean enabled() {
        return GTMQoLConfig.get().machines.probableDevices;
    }

    /** Call during mod construction. */
    public static void init() {
        if (!enabled()) return;
        IMPROBABILITY = device("probable_improbability_device", "Probable Improbability Device", IV, false,
                "Chanced inputs are never consumed, and ranged inputs use their smallest amount.");
        CERTAINTY = device("probable_certainty_device", "Probable Certainty Device", ZPM, true,
                "Chanced outputs always come out, and ranged outputs give their largest amount.");
    }

    private static MachineEntry<MachineDefinition> device(String id, String name, int tier, boolean certainty,
                                                          String tooltip) {
        var entry = GTMQoLAddon.machine(id, info -> new ProbableDevicePartMachine(info, certainty))
                .rotationState(RotationState.ALL)
                .tier(tier)
                .abilities(PROBABLE_DEVICE)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(GTMQoL.id("block/overlay/machine/" + id))
                .langValue(name)
                .register();
        // gtceu shows <namespace>.machine.<id>.tooltip on its own
        GTMQoLAddon.registrate().addRawLang(GTMQoL.MOD_ID + ".machine." + id + ".tooltip", tooltip);
        return entry;
    }

    public static void addRecipes(RecipeOutput provider) {
        if (IMPROBABILITY == null || CERTAINTY == null) return;
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("probable_improbability_device"))
                .inputItems(GTMachines.HULL[IV].asStack())
                .inputItems(GTItems.FIELD_GENERATOR_EV, 16)
                .inputItems(CustomTags.LuV_CIRCUITS, 64)
                .inputItems(GTItems.ROBOT_ARM_IV, 4)
                .inputFluids(Polybenzimidazole.getFluid(L * 4))
                .outputItems(IMPROBABILITY.asStack())
                .duration(2000)
                .EUt(VA[IV])
                .save(provider);
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("probable_certainty_device"))
                .inputItems(GTMachines.HULL[ZPM].asStack())
                .inputItems(GTItems.FIELD_GENERATOR_ZPM, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 64)
                .inputItems(GTItems.FLUID_REGULATOR_ZPM, 4)
                .inputFluids(Naquadria.getFluid(L * 4))
                .outputItems(CERTAINTY.asStack())
                .duration(2000)
                .EUt(VA[ZPM])
                .save(provider);
    }
}
