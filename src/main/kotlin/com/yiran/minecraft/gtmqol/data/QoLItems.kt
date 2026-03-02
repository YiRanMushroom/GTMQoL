package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.tterrag.registrate.util.entry.ItemEntry
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import net.minecraft.world.item.Item
import java.util.Locale.ENGLISH

object QoLItems {
    val SILICON_CHIP: ItemEntry<Item>
    val PHOSPHORUS_DOPED_SILICON_CHIP: ItemEntry<Item>
    val NAQUADAH_DOPED_SILICON_CHIP: ItemEntry<Item>
    val NEUTRONIUM_DOPED_SILICON_CHIP: ItemEntry<Item>

    val UNIVERSAL_CIRCUITS: Array<ItemEntry<Item>>

    fun circuitTiers(): Array<Int> {
        if (GTCEuAPI.isHighTier()) {
            return (0..9).toList().toTypedArray()
        } else {
            return GTValues.ALL_TIERS.toList().toTypedArray()
        }
    }

    init {
        SILICON_CHIP = GTMQoLRegistrate.REGISTRATE.item("silicon_chip", ::Item)
            .lang("Silicon Chip").register()
        PHOSPHORUS_DOPED_SILICON_CHIP = GTMQoLRegistrate.REGISTRATE.item("phosphorus_doped_silicon_chip", ::Item)
            .lang("Phosphorus Doped Silicon Chip").register()
        NAQUADAH_DOPED_SILICON_CHIP = GTMQoLRegistrate.REGISTRATE.item("naquadah_doped_silicon_chip", ::Item)
            .lang("Naquadah Doped Silicon Chip").register()
        NEUTRONIUM_DOPED_SILICON_CHIP = GTMQoLRegistrate.REGISTRATE.item("neutronium_doped_silicon_chip", ::Item)
            .lang("Neutronium Doped Silicon Chip").register()

        UNIVERSAL_CIRCUITS = circuitTiers().map {
            GTMQoLRegistrate.REGISTRATE.item("universal_circuits/${GTValues.VN[it].lowercase(ENGLISH)}_universal_circuit", ::Item)
                .lang("${GTValues.VN[it]} Universal Circuit").register() as ItemEntry<Item>
        }.toTypedArray()
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLItems object.
    }
}