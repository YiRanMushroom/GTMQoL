package com.yiran.minecraft.gtmqol.data

import com.yiran.minecraft.gtmqol.GTMQoL
import com.yiran.minecraft.gtmqol.mekanismPresentedAndIntegrationEnabled
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraftforge.event.TagsUpdatedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Suppress("UNUSED")
@Mod.EventBusSubscriber(modid = GTMQoL.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object RuntimeTagHandler {
    @SubscribeEvent
    @JvmStatic
    fun onTagsUpdated(event: TagsUpdatedEvent) {

        if (!mekanismPresentedAndIntegrationEnabled()) return

        val registryManager = event.registryAccess
        val itemRegistry = registryManager.registryOrThrow(Registries.ITEM)

        val conversionPairs = listOf(
            createLocation("gtceu", "circuits/lv") to createLocation("forge", "circuits/basic"),
            createLocation("gtceu", "circuits/mv") to createLocation("forge", "circuits/advanced"),
            createLocation("gtceu", "circuits/hv") to createLocation("forge", "circuits/elite"),
            createLocation("gtceu", "circuits/ev") to createLocation("forge", "circuits/ultimate")
        )

        conversionPairs.forEach { (gtLoc, forgeLoc) ->
            val gtTagKey = ItemTags.create(gtLoc)
            val forgeTagKey = ItemTags.create(forgeLoc)

            val gtHolderSet = itemRegistry.getOrCreateTag(gtTagKey)
            val forgeHolderSet = itemRegistry.getOrCreateTag(forgeTagKey)

            val combinedList = mutableListOf<Holder<Item>>()

            gtHolderSet.forEach { combinedList.add(it) }
            forgeHolderSet.forEach { combinedList.add(it) }

            val distinctHolders = combinedList.distinct()

            gtHolderSet.bind(distinctHolders)
            forgeHolderSet.bind(distinctHolders)
        }

    }

    private fun createLocation(namespace: String, path: String): ResourceLocation {
        return ResourceLocation.tryBuild(namespace, path)!!
    }
}