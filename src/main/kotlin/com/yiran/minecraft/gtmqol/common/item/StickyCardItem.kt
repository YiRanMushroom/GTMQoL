package com.yiran.minecraft.gtmqol.common.item

import appeng.api.upgrades.Upgrades
import appeng.core.definitions.AEParts
import appeng.items.materials.UpgradeCardItem
import com.lowdragmc.lowdraglib.LDLib
import com.yiran.minecraft.gtmqol.config.ConfigHolder
import com.yiran.minecraft.gtmqol.data.QoLItems
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class StickyCardItem(properties: Item.Properties) : UpgradeCardItem(properties) {
    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        lines: MutableList<Component>,
        advancedTooltips: TooltipFlag
    ) {
        super.appendHoverText(stack, level, lines, advancedTooltips)

        lines.add(Component.translatable("gtmqol.item.sticky_card.tooltip.0"))
        lines.add(Component.translatable("gtmqol.item.sticky_card.tooltip.1"))
        lines.add(Component.translatable("gtmqol.item.sticky_card.tooltip.2"))
        lines.add(Component.translatable("gtmqol.item.sticky_card.tooltip.3"))
    }

    companion object {
        @JvmStatic
        fun shouldAct() : Boolean {
            return LDLib.isModLoaded("ae2") && ConfigHolder.instance.addonConfig.enableAEStickyCard
        }

        @JvmStatic
        fun onCommonSetup() {
            if (shouldAct()) {
                val storageGroup = "group.storage.name"
                Upgrades.add(QoLItems.STICKY_CARD_ITEM.get(), AEParts.STORAGE_BUS, 1, storageGroup)
            }
        }
    }
}