package com.yiran.minecraft.gtmqol.common.assembler;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.circuit.UniversalCircuits;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

/**
 * Assembler for this mod's own recipes (universal circuits, wireless parts, creative data access hatch).
 */
public final class MagicalAssembler {

    public static GTRecipeType RECIPE_TYPE;
    public static MachineDefinition[] MACHINES;

    private MagicalAssembler() {}

    /** From GTCEu's recipe type {@code RegisterEvent}. */
    public static void initRecipeType() {
        RECIPE_TYPE = GTRecipeTypes.register(GTMQoL.id("magical_assembler"), GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(16, 1, 4, 1)
                .setEUIO(IO.IN)
                .prepareBuilder(builder -> builder.EUt(VA[LV]))
                .UI(MagicalAssemblerUI::apply)
                .setSound(GTSoundEntries.SCIENCE);
        GTMQoLAddon.registrate().addRawLang(RECIPE_TYPE.registryName.toLanguageKey(), "Magical Assembler");
    }

    /** From GTCEu's machine {@code RegisterEvent}. */
    public static void initMachines() {
        MACHINES = new GTMachineUtils.SimpleMachineBuilder(GTMQoLAddon.registrate(), "magical_assembler",
                RECIPE_TYPE).register();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        registerMachineRecipe(provider, MACHINES,
                "PGP", "GMG", "PCP",
                'M', HULL,
                'G', Items.GLASS,
                'C', CIRCUIT,
                'P', PLATE);

        if (GTMQoLConfig.get().circuits.universalCircuits) {
            for (int tier : ALL_TIERS) {
                RECIPE_TYPE.recipeBuilder(GTMQoL.id("circuit_conversion_tier_" + tier))
                        .inputItems(CustomTags.CIRCUITS_ARRAY[tier])
                        .outputItems(UniversalCircuits.UNIVERSAL_CIRCUITS[tier])
                        .circuitMeta(5)
                        .duration(1)
                        .EUt(1)
                        .save(provider);
            }
        }

        RECIPE_TYPE.recipeBuilder(GTMQoL.id("produce_creative_data_access_hatch"))
                .inputItems(GTResearchMachines.RESEARCH_STATION)
                .inputItems(GTResearchMachines.DATA_BANK, 4)
                .inputItems(GTResearchMachines.NETWORK_SWITCH, 16)
                .inputItems(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY, 64)
                .inputItems(GTResearchMachines.HPCA_BRIDGE_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_COMPUTATION_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 64)
                .inputItems(GTResearchMachines.HPCA_ACTIVE_COOLER_COMPONENT, 64)
                .inputItems(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH)
                .inputFluids(Lubricant, 1000 * 64)
                .inputFluids(PCBCoolant, 1000 * 64)
                .inputFluids(Polybenzimidazole, 144 * 64)
                .inputFluids(Tritanium, 144 * 64)
                .outputItems(GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH)
                .duration(20 * 60 * 20)
                .EUt(VA[ZPM])
                .save(provider);

        RECIPE_TYPE.recipeBuilder(GTMQoL.id("copy_creative_data_access_hatch"))
                .notConsumable(GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH.asStack())
                .inputItems(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH)
                .outputItems(GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH)
                .circuitMeta(10)
                .duration(20 * 20)
                .EUt(VA[ZPM])
                .save(provider);

        RECIPE_TYPE.recipeBuilder(GTMQoL.id("rubber_sapling"))
                .inputItems(ItemTags.SAPLINGS)
                .inputItems(GTItems.STICKY_RESIN)
                .outputItems(GTBlocks.RUBBER_SAPLING.asItem())
                .duration(100)
                .EUt(VA[ULV])
                .save(provider);
    }
}
