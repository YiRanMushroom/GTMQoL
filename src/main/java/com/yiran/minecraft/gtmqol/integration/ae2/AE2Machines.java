package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Glue;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SolderingAlloy;

/** AE2 machines. Only touch this class when AE2 is loaded ({@code GTCEu.Mods.isAE2Loaded()}). */
public final class AE2Machines {

    public static MachineEntry<MachineDefinition> OVERCLOCKED_ME_PATTERN_BUFFER;

    private AE2Machines() {}

    public static void init() {
        OVERCLOCKED_ME_PATTERN_BUFFER = GTMQoLAddon
                .machine("overclocked_me_pattern_buffer", info -> new AbstractMEPatternBufferPartMachine(info) {

                    @Override
                    public int getPatternColumns() {
                        return 12;
                    }

                    @Override
                    public int getPatternRows() {
                        return 18;
                    }
                })
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .langValue("Overclocked ME Pattern Buffer")
                .tooltips(Component.translatable("block.gtceu.pattern_buffer.desc.0"),
                        Component.translatable("block.gtceu.pattern_buffer.desc.1"),
                        Component.translatable("block.gtceu.pattern_buffer.desc.2"),
                        Component.translatable("gtceu.part_sharing.enabled"))
                .register();

        // SmartDoubling's keys are compile-time constants, so this doesn't load it (or ExtendedAE Plus).
        addLang(SmartDoubling.TITLE_KEY, "Smart Doubling");
        addLang(SmartDoubling.TOGGLE_KEY + ".enabled", "Smart doubling: on");
        addLang(SmartDoubling.TOGGLE_KEY + ".disabled", "Smart doubling: off");
        addLang(SmartDoubling.LIMIT_KEY, "Limit (0 = none)");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }

    public static void addRecipes(RecipeOutput provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("overclocked_me_pattern_buffer"))
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER, 4)
                .inputItems(CustomTags.MV_CIRCUITS, 16)
                .circuitMeta(24)
                .inputFluids(SolderingAlloy, L * 4)
                .inputFluids(Glue, 4000)
                .outputItems(OVERCLOCKED_ME_PATTERN_BUFFER)
                .duration(1200)
                .EUt(VA[MV])
                .save(provider);
    }
}
