package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.GTValues.LV
import com.gregtechceu.gtceu.api.GTValues.VA
import com.gregtechceu.gtceu.api.data.tag.TagPrefix.lens
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.common.data.GTMachines
import com.gregtechceu.gtceu.common.data.GTMachines.*
import com.gregtechceu.gtceu.common.data.GTMaterials.Diamond
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents
import com.hepdd.gtmthings.GTMThings
import com.hepdd.gtmthings.data.CustomItems
import com.hepdd.gtmthings.data.WirelessMachines
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.gtmthings.WirelessEnergyAccessor
import net.minecraft.data.recipes.FinishedRecipe
import java.util.*
import java.util.function.Consumer


object GTMRecipeGen {
    fun initGTMRecipes(provider: Consumer<FinishedRecipe>) {
        val MAGIC_ASSEMBLER_RECIPES = QoLRecipeTypes.MAGICAL_ASSEMBLER_RECIPES!!
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

        for (tier in GTValues.tiersBetween(
            if (ConfigHolder.instance.addonConfig.registerLaserHatchesForAllTiers) GTValues.EV else GTValues.IV,
            if (GTCEuAPI.isHighTier()) GTValues.OpV else GTValues.UV
        )) {
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

            if (ConfigHolder.instance.addonConfig.enableHigherAmpLaserHatches) {
                // Wireless Energy Input Hatch 16384A: laser input hatch 16384A + circuit 16, LV 200t
                MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                    GTMThings.id(
                        "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                            Locale.getDefault()
                        ) + "_16384a"

                    )
                ).inputItems(QoLMachines.HIGH_AMP_LASERS?.get(16384)?.first?.get(tier)?.asStack()!!)
                    .circuitMeta(5)
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16384A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV].toLong())
                    .save(provider)

                MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                    GTMThings.id(
                        "wireless_energy_input_hatch_" + GTValues.VN[tier].lowercase(
                            Locale.getDefault()
                        ) + "_65536a"

                    )
                ).inputItems(QoLMachines.HIGH_AMP_LASERS?.get(65536)?.first?.get(tier)?.asStack()!!)
                    .circuitMeta(5)
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_65536A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV].toLong())
                    .save(provider)
            }


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

            if (ConfigHolder.instance.addonConfig.enableHigherAmpLaserHatches) {
                // Wireless Energy Output Hatch 16384A: laser output hatch 16384A + circuit 16, LV 200t
                MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                    GTMThings.id(
                        "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                            Locale.getDefault()
                        ) + "_16384a"

                    )
                ).inputItems(QoLMachines.HIGH_AMP_LASERS?.get(16384)?.second?.get(tier)?.asStack()!!)
                    .circuitMeta(5)
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16384A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV].toLong())
                    .save(provider)

                MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                    GTMThings.id(
                        "wireless_energy_output_hatch_" + GTValues.VN[tier].lowercase(
                            Locale.getDefault()
                        ) + "_65536a"

                    )
                ).inputItems(QoLMachines.HIGH_AMP_LASERS?.get(65536)?.second?.get(tier)?.asStack()!!)
                    .circuitMeta(5)
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_65536A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[GTValues.LV].toLong())
                    .save(provider)
            }

            MAGIC_ASSEMBLER_RECIPES.recipeBuilder(
                GTMThings.id(
                    "simple_wireless_energy_monitor"
                )
            ).inputItems(GTMachines.HULL[LV].asStack())
                .inputItems(CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A.asStack())
                .outputItems(WirelessMachines.WIRELESS_ENERGY_MONITOR)
                .circuitMeta(5)
                .duration(200)
                .EUt(GTValues.VA[GTValues.LV].toLong())
                .save(provider)
        }
    }

    fun overrideLaserRecipes(provider: Consumer<FinishedRecipe>) {

        // 256A Laser Target Hatches
        for (tier in 0..<LASER_INPUT_HATCH_256.size) {
            val hatch =
                LASER_INPUT_HATCH_256[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_256a_laser_target_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond)
                .inputItems(GTCraftingComponents.SENSOR.get(tier))
                .inputItems(GTCraftingComponents.PUMP.get(tier))
                .inputItems(GTCraftingComponents.CABLE.get(tier), 4)
                .circuitMeta(1)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }


        // 256A Laser Source Hatches
        for (tier in 0..<LASER_OUTPUT_HATCH_256.size) {
            val hatch =
                LASER_OUTPUT_HATCH_256[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_256a_laser_source_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond)
                .inputItems(GTCraftingComponents.EMITTER.get(tier))
                .inputItems(GTCraftingComponents.PUMP.get(tier))
                .inputItems(GTCraftingComponents.CABLE.get(tier), 4)
                .circuitMeta(1)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }


        // 1024A Laser Target Hatches
        for (tier in 0..<LASER_INPUT_HATCH_1024.size) {
            val hatch =
                LASER_INPUT_HATCH_1024[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_1024a_laser_target_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.SENSOR.get(tier), 2)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 2)
                .inputItems(GTCraftingComponents.CABLE_DOUBLE.get(tier), 4)
                .circuitMeta(2)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }


        // 1024A Laser Source Hatches
        for (tier in 0..<LASER_OUTPUT_HATCH_1024.size) {
            val hatch =
                LASER_OUTPUT_HATCH_1024[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_1024a_laser_source_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.EMITTER.get(tier), 2)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 2)
                .inputItems(GTCraftingComponents.CABLE_DOUBLE.get(tier), 4)
                .circuitMeta(2)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }


        // 4096A Laser Target Hatches
        for (tier in 0..<LASER_INPUT_HATCH_4096.size) {
            val hatch =
                LASER_INPUT_HATCH_4096[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_4096a_laser_target_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.SENSOR.get(tier), 3)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 3)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(3)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }


        // 4096A Laser Source Hatches
        for (tier in 0..<LASER_OUTPUT_HATCH_4096.size) {

            val hatch: MachineDefinition =
                LASER_OUTPUT_HATCH_4096[tier] ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_4096a_laser_output_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.EMITTER.get(tier), 3)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 3)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(3)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }

        for (tier in 0..<(QoLMachines.HIGH_AMP_LASERS?.get(16384)?.first?.size ?: 0)) {
            val hatch = QoLMachines.HIGH_AMP_LASERS?.get(16384)?.first?.get(tier) ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_16384a_laser_target_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.SENSOR.get(tier), 4)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 4)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(4)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }

        for (tier in 0..<(QoLMachines.HIGH_AMP_LASERS?.get(16384)?.second?.size ?: 0)) {
            val hatch = QoLMachines.HIGH_AMP_LASERS?.get(16384)?.second?.get(tier) ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_16384a_laser_output_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.EMITTER.get(tier), 4)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 4)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(4)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }

        for (tier in 0..<(QoLMachines.HIGH_AMP_LASERS?.get(65536)?.first?.size ?: 0)) {
            val hatch = QoLMachines.HIGH_AMP_LASERS?.get(65536)?.first?.get(tier) ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_65536a_laser_target_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.SENSOR.get(tier), 5)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 5)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(5)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }

        for (tier in 0..<(QoLMachines.HIGH_AMP_LASERS?.get(65536)?.second?.size ?: 0)) {
            val hatch = QoLMachines.HIGH_AMP_LASERS?.get(65536)?.second?.get(tier) ?: continue

            ASSEMBLER_RECIPES.recipeBuilder(GTValues.VN[tier].lowercase() + "_65536a_laser_output_hatch")
                .inputItems(HULL[tier])
                .inputItems(lens, Diamond, 1)
                .inputItems(GTCraftingComponents.EMITTER.get(tier), 5)
                .inputItems(GTCraftingComponents.PUMP.get(tier), 5)
                .inputItems(GTCraftingComponents.CABLE_QUAD.get(tier), 4)
                .circuitMeta(5)
                .outputItems(hatch)
                .duration(300).EUt(VA[tier].toLong())
                .addMaterialInfo(true).save(provider)
        }
    }
}