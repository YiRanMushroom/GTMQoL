package com.yiran.minecraft.gtmqol.wireless;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.wireless.steam.WirelessSteamMachines;

import net.minecraft.data.recipes.RecipeOutput;

import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Magical assembler recipes for the wireless parts. Every part comes from a plain hatch: GT hatch + circuit 5
 * gives the wireless hatch, the wireless hatch gives 4 covers, a GT hatch + 4 covers gives the accessor. Steam
 * follows the same scheme starting from GT's steam hatch (circuit 6 for the output side, since GT has no
 * steam output hatch).
 */
public final class WirelessRecipes {

    private WirelessRecipes() {}

    public static void addRecipes(RecipeOutput provider) {
        var assembler = MagicalAssembler.RECIPE_TYPE;

        for (int tier : GTMachineUtils.ALL_TIERS) {
            if (GTMachines.ENERGY_INPUT_HATCH[tier] == null || GTMachines.ENERGY_OUTPUT_HATCH[tier] == null) {
                continue;
            }
            String tierName = VN[tier].toLowerCase(Locale.ROOT);

            assembler.recipeBuilder(GTMQoL.id("wireless_energy_input_hatch_" + tierName))
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier])
                    .circuitMeta(5)
                    .outputItems(WirelessEnergyMachines.ENERGY_INPUT_HATCH[tier])
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);

            assembler.recipeBuilder(GTMQoL.id("wireless_energy_output_hatch_" + tierName))
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier])
                    .circuitMeta(5)
                    .outputItems(WirelessEnergyMachines.ENERGY_OUTPUT_HATCH[tier])
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);

            assembler.recipeBuilder(GTMQoL.id("wireless_energy_input_cover_" + tierName))
                    .inputItems(WirelessEnergyMachines.ENERGY_INPUT_HATCH[tier])
                    .circuitMeta(5)
                    .outputItems(WirelessCovers.ENERGY_INPUT_ITEM[tier], 4)
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);

            assembler.recipeBuilder(GTMQoL.id("wireless_energy_output_cover_" + tierName))
                    .inputItems(WirelessEnergyMachines.ENERGY_OUTPUT_HATCH[tier])
                    .circuitMeta(5)
                    .outputItems(WirelessCovers.ENERGY_OUTPUT_ITEM[tier], 4)
                    .duration(200)
                    .EUt(VA[LV])
                    .save(provider);

            assembler.recipeBuilder(GTMQoL.id("wireless_energy_accessor_" + tierName))
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier])
                    .inputItems(WirelessCovers.ENERGY_INPUT_ITEM[tier], 4)
                    .outputItems(WirelessEnergyMachines.ENERGY_ACCESSOR[tier])
                    .duration(400)
                    .EUt(VA[LV])
                    .save(provider);
        }

        assembler.recipeBuilder(GTMQoL.id("wireless_energy_monitor"))
                .inputItems(GTItems.COVER_SCREEN)
                .inputItems(WirelessCovers.ENERGY_INPUT_ITEM[LV])
                .outputItems(WirelessEnergyMachines.ENERGY_MONITOR)
                .duration(400)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_input_hatch"))
                .inputItems(GTMachines.STEAM_HATCH)
                .circuitMeta(5)
                .outputItems(WirelessSteamMachines.STEAM_INPUT_HATCH)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_output_hatch"))
                .inputItems(GTMachines.STEAM_HATCH)
                .circuitMeta(6)
                .outputItems(WirelessSteamMachines.STEAM_OUTPUT_HATCH)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_input_cover"))
                .inputItems(WirelessSteamMachines.STEAM_INPUT_HATCH)
                .circuitMeta(5)
                .outputItems(WirelessCovers.STEAM_INPUT_ITEM, 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_output_cover"))
                .inputItems(WirelessSteamMachines.STEAM_OUTPUT_HATCH)
                .circuitMeta(5)
                .outputItems(WirelessCovers.STEAM_OUTPUT_ITEM, 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_accessor"))
                .inputItems(GTMachines.STEAM_HATCH)
                .inputItems(WirelessCovers.STEAM_INPUT_ITEM, 4)
                .outputItems(WirelessSteamMachines.STEAM_ACCESSOR)
                .duration(400)
                .EUt(VA[LV])
                .save(provider);

        assembler.recipeBuilder(GTMQoL.id("wireless_steam_monitor"))
                .inputItems(GTItems.COVER_SCREEN)
                .inputItems(WirelessCovers.STEAM_INPUT_ITEM)
                .outputItems(WirelessSteamMachines.STEAM_MONITOR)
                .duration(400)
                .EUt(VA[LV])
                .save(provider);
    }
}
