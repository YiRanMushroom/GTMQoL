package com.yiran.minecraft.gtmqol.logic

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraftforge.registries.ForgeRegistries


object RegistryUtils {
    fun resourceLocationOf(namespace: String, path: String): ResourceLocation {
        return ResourceLocation.tryBuild(namespace, path)!!
    }

    fun Item.resourceLocation(): ResourceLocation? {
        return ForgeRegistries.ITEMS.getKey(this)
    }

    fun ResourceLocation.asItem(): Item? {
        return ForgeRegistries.ITEMS.getValue(this)
            ?.takeIf { it !== Items.AIR }
    }

    fun String.atNamespace(namespace: String): ResourceLocation {
        return resourceLocationOf(namespace, this)
    }

    fun String.atPath(path: String): ResourceLocation {
        return resourceLocationOf(this, path)
    }

    fun String.addSuffix(suffix: String): String {
        return this + suffix
    }
}