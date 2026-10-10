package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Glue;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SolderingAlloy;

/**
 * ME dual input / output parts (LuV, like the other ME buses and the pattern buffer) and the lang of the
 * pattern buffer's "return to network" switch. Only touch this class when AE2 is loaded.
 */
public final class AEDualParts {

    public static MachineEntry<MachineDefinition> ME_DUAL_INPUT;
    public static MachineEntry<MachineDefinition> ME_DUAL_OUTPUT;
    public static MachineEntry<MachineDefinition> ME_UNIVERSAL_INPUT;
    public static MachineEntry<MachineDefinition> ME_UNIVERSAL_OUTPUT;

    private static final String UNIVERSAL_INPUT_TOOLTIP = "gtmqol.machine.me_universal_input.tooltip";
    private static final String UNIVERSAL_OUTPUT_TOOLTIP = "gtmqol.machine.me_universal_output.tooltip";

    private AEDualParts() {}

    public static void initLang() {
        // Constant, so this doesn't load PatternBufferReturn.
        GTMQoLAddon.registrate().addRawLang(PatternBufferReturn.TOGGLE_KEY + ".enabled",
                "Outputs go to the ME network: on");
        GTMQoLAddon.registrate().addRawLang(PatternBufferReturn.TOGGLE_KEY + ".disabled",
                "Outputs go to the ME network: off");
    }

    public static void init() {
        ME_DUAL_INPUT = GTMQoLAddon.machine("me_dual_input", MEDualInputPartMachine::new)
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_input_bus"))
                .langValue("ME Dual Input")
                .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                        Component.translatable("gtceu.machine.me.item_import.tooltip"),
                        Component.translatable("gtceu.machine.me.copy_paste.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();

        ME_DUAL_OUTPUT = GTMQoLAddon.machine("me_dual_output", MEDualOutputPartMachine::new)
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_output_bus"))
                .langValue("ME Dual Output")
                .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                        Component.translatable("gtceu.machine.me.export.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();

        ME_UNIVERSAL_INPUT = GTMQoLAddon.machine("me_universal_input", MEUniversalInputPartMachine::new)
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_input_bus"))
                .langValue("ME Universal Input")
                .tooltips(Component.translatable(UNIVERSAL_INPUT_TOOLTIP),
                        Component.translatable("gtceu.machine.me.item_import.tooltip"),
                        Component.translatable("gtceu.machine.me.copy_paste.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();

        ME_UNIVERSAL_OUTPUT = GTMQoLAddon.machine("me_universal_output", MEUniversalOutputPartMachine::new)
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_output_bus"))
                .langValue("ME Universal Output")
                .tooltips(Component.translatable(UNIVERSAL_OUTPUT_TOOLTIP),
                        Component.translatable("gtceu.machine.me.export.tooltip"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();

        GTMQoLAddon.registrate().addRawLang(MEDualInputPartMachine.FLUIDS_KEY, "Fluids");
        GTMQoLAddon.registrate().addRawLang(MEDualInputPartMachine.ITEMS_KEY, "Items");
        GTMQoLAddon.registrate().addRawLang(UNIVERSAL_INPUT_TOOLTIP,
                "Stocks items, fluids and other ME types (e.g. Mekanism chemicals) in 36 slots");
        GTMQoLAddon.registrate().addRawLang(UNIVERSAL_OUTPUT_TOOLTIP,
                "Outputs items, fluids and other ME types (e.g. Mekanism chemicals) to the ME network");
    }

    public static void addRecipes(RecipeOutput provider, boolean meAssembler) {
        GTRecipeTypeEntry type = meAssembler ? AEProcessing.ME_ASSEMBLER_RECIPES :
                GTRecipeTypes.ASSEMBLER_RECIPES;

        type.recipeBuilder(GTMQoL.id("me_dual_input"))
                .inputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME)
                .inputItems(GTAEMachines.STOCKING_IMPORT_HATCH_ME)
                .inputFluids(Glue, 1000)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ME_DUAL_INPUT)
                .duration(400)
                .EUt(VA[IV])
                .save(provider);

        type.recipeBuilder(GTMQoL.id("me_dual_output"))
                .inputItems(GTAEMachines.ITEM_EXPORT_BUS_ME)
                .inputItems(GTAEMachines.FLUID_EXPORT_HATCH_ME)
                .inputFluids(Glue, 1000)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ME_DUAL_OUTPUT)
                .duration(400)
                .EUt(VA[IV])
                .save(provider);

        type.recipeBuilder(GTMQoL.id("me_universal_input"))
                .inputItems(ME_DUAL_INPUT)
                .inputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME)
                .inputFluids(Glue, 1000)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ME_UNIVERSAL_INPUT)
                .duration(400)
                .EUt(VA[IV])
                .save(provider);

        type.recipeBuilder(GTMQoL.id("me_universal_output"))
                .inputItems(ME_DUAL_OUTPUT)
                .inputItems(GTAEMachines.ITEM_EXPORT_BUS_ME)
                .inputFluids(Glue, 1000)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ME_UNIVERSAL_OUTPUT)
                .duration(400)
                .EUt(VA[IV])
                .save(provider);
    }
}
