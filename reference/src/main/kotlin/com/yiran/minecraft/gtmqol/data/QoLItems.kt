package com.yiran.minecraft.gtmqol.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.data.recipe.CustomTags
import com.tterrag.registrate.util.entry.ItemEntry
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.common.item.PCBRecipeModifierProviderItem
import com.yiran.minecraft.gtmqol.common.item.StickyCardItem
import net.minecraft.world.item.Item
import java.util.Locale.ENGLISH

object QoLItems {
    val SILICON_CHIP: ItemEntry<Item>
    val PHOSPHORUS_DOPED_SILICON_CHIP: ItemEntry<Item>
    val NAQUADAH_DOPED_SILICON_CHIP: ItemEntry<Item>
    val NEUTRONIUM_DOPED_SILICON_CHIP: ItemEntry<Item>

    @JvmStatic
    val UNIVERSAL_CIRCUITS: Array<ItemEntry<Item>>

    lateinit var TITANIUM_NANITE: ItemEntry<Item>
    lateinit var OSMIRIDIUM_NANITE: ItemEntry<Item>
    lateinit var NEUTRONIUM_NANITE: ItemEntry<Item>

    lateinit var STICKY_CARD_ITEM: ItemEntry<StickyCardItem>

    fun circuitTiers(): Array<Int> {
        if (GTCEuAPI.isHighTier()) {
            return (GTValues.ULV..GTValues.MAX).toList().toTypedArray()
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
            GTMQoLRegistrate.REGISTRATE.item("${GTValues.VN[it].lowercase(ENGLISH)}_universal_circuit", ::Item)
                .lang("${GTValues.VN[it]} Universal Circuit")
                .tag(CustomTags.CIRCUITS_ARRAY[it])
                .register() as ItemEntry<Item>
        }.toTypedArray()

        TITANIUM_NANITE =
            GTMQoLRegistrate.REGISTRATE.item("titanium_nanite", PCBRecipeModifierProviderItem.factory(2, 4))
                .lang("Titanium Nanite").register()

        OSMIRIDIUM_NANITE =
            GTMQoLRegistrate.REGISTRATE.item("osmiridium_nanite", PCBRecipeModifierProviderItem.factory(4, 4))
                .lang("Osmiridium Nanite").register()

        NEUTRONIUM_NANITE =
            GTMQoLRegistrate.REGISTRATE.item("neutronium_nanite", PCBRecipeModifierProviderItem.factory(4, 2))
                .lang("Neutronium Nanite").register()

        if (StickyCardItem.shouldAct()) {
            STICKY_CARD_ITEM = GTMQoLRegistrate.REGISTRATE.item("sticky_card", ::StickyCardItem)
                .lang("Sticky Card").register()
        }
    }

    @JvmStatic
    fun init() {
        // This function is intentionally left blank. It serves as an initializer for the QoLItems object.
    }
}