package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.item.ComponentItem
import com.gregtechceu.gtceu.common.data.GTMachines
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.data.CustomItems
import com.hepdd.gtmthings.data.WirelessMachines
import com.tterrag.registrate.util.entry.ItemEntry
import com.yiran.minecraft.gtmqol.gtmthings.WirelessEnergyAccessor
import net.minecraft.data.recipes.FinishedRecipe
import java.util.*
import java.util.function.Consumer


object GTMRecipeGen {
    fun initGTMRecipes(provider: Consumer<FinishedRecipe>) {
        val MAGIC_ASSEMBLER_RECIPES = QoLRecipeTypes.MAGICAL_ASSEMBLER!!
        MAGIC_ASSEMBLER_RECIPES.recipeBuilder(GTMThings.id("wireless_energy_interface"))
            .inputItems(GTMachines.ENERGY_INPUT_HATCH[1].asStack())
            .inputItems(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A.asStack(4))
            .outputItems(WirelessMachines.WIRELESS_ENERGY_INTERFACE.asStack())
            .duration(400)
            .EUt(GTValues.VA[GTValues.LV].toLong())
            .save(provider);

        MAGIC_ASSEMBLER_RECIPES.recipeBuilder(GTMThings.id("wireless_energy_accessor"))
            .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[1].asStack())
            .inputItems(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A.asStack(4))
            .outputItems(WirelessEnergyAccessor.INSTANCE.asStack())
            .duration(400)
            .EUt(GTValues.VA[GTValues.LV].toLong())
            .save(provider);


        val WIRELESS_ENERGY_RECEIVE_COVER =
            mutableListOf(
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV
            )

        if (GTCEuAPI.isHighTier()) {
            WIRELESS_ENERGY_RECEIVE_COVER.addAll(
                mutableListOf(
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV
                )
            )
        }

        val WIRELESS_ENERGY_RECEIVE_COVER_4A =
            mutableListOf(
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV_4A
            )

        if (GTCEuAPI.isHighTier()) {
            WIRELESS_ENERGY_RECEIVE_COVER_4A.addAll(
                mutableListOf(
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A,
                    CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A
                )
            )
        }

        for (tier in GTValues.tiersBetween(GTValues.LV, if (GTCEuAPI.isHighTier()) GTValues.OpV else GTValues.UV)) {
            // Wireless Energy Input Hatch: energy input hatch + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    )
                )
            )
                .inputItems(GTMachines.ENERGY_INPUT_HATCH[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch: energy output hatch + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    )
                )
            )
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Receive Cover from Input Hatch: wireless input hatch + circuit 18, 1 to 8
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_receive_cover_from_input_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    )
                )
            )
                .inputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH[tier].asStack())
                .circuitMeta(5)
                .outputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1)!!.asStack(4))
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Receive Cover from Output Hatch: wireless output hatch + circuit 18, 1 to 8
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_receive_cover_from_output_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    )
                )
            )
                .inputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH[tier].asStack())
                .circuitMeta(5)
                .outputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1)!!.asStack(4))
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)
        }

        for (tier in GTValues.tiersBetween(GTValues.EV, if (GTCEuAPI.isHighTier()) GTValues.OpV else GTValues.UV)) {

            // Wireless Energy Input Hatch 4A: energy input hatch 4A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_4a"
                )
            )
                .inputItems(GTMachines.ENERGY_INPUT_HATCH_4A[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Input Hatch 16A: energy input hatch 16A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_16a"
                )
            )
                .inputItems(GTMachines.ENERGY_INPUT_HATCH_16A[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch 4A: energy output hatch 4A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_4a"
                )
            )
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_4A[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch 16A: energy output hatch 16A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_16a"
                )
            )
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_16A[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)
        }

        for (tier in GTValues.tiersBetween(GTValues.IV, if (GTCEuAPI.isHighTier()) GTValues.OpV else GTValues.UV)) {
            // Wireless Energy Input Hatch 256A: laser input hatch 256A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_256a"
                )
            )
                .inputItems(GTMachines.LASER_INPUT_HATCH_256[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_256A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Input Hatch 1024A: laser input hatch 1024A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_1024a"
                )
            )
                .inputItems(GTMachines.LASER_INPUT_HATCH_1024[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_1024A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Input Hatch 4096A: laser input hatch 4096A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_4096a"
                )
            )
                .inputItems(GTMachines.LASER_INPUT_HATCH_4096[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4096A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch 256A: laser output hatch 256A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_256a"
                )
            )
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_256[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_256A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch 1024A: laser output hatch 1024A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_1024a"
                )
            )
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_1024[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_1024A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)

            // Wireless Energy Output Hatch 4096A: laser output hatch 4096A + circuit 16, LV 200t
            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                        Locale.getDefault()
                    ) + "_4096a"
                )
            )
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_4096[tier].asStack())
                .circuitMeta(5)
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4096A[tier].asStack())
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)
        }
    }
}