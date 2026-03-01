package com.yiran.minecraft.gtmqol

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs
import com.tterrag.registrate.util.entry.RegistryEntry
import com.yiran.minecraft.gtmqol.data.QoLMultiblocks
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab

object GTMQoLRegistrate {
    @JvmStatic
    val REGISTRATE: GTRegistrate = GTRegistrate.create(GTMQoL.MOD_ID).apply {

        MAIN_TAB = this.defaultCreativeTab("main") { builder ->
            builder
                .displayItems(GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", REGISTRATE))
                .title(Component.translatable("itemGroup.gtmqol.main"))
                .icon { QoLMultiblocks.SMART_ASSEMBLY_FACTORY?.asStack() }
                .build()
        }.register()

        this.creativeModeTab(MAIN_TAB)

    }

    @JvmStatic
    val MAIN_TAB: RegistryEntry<CreativeModeTab>
}