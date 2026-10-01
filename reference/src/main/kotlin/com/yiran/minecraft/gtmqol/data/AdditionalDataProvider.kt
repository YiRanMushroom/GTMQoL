package com.yiran.minecraft.gtmqol.data

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

object AdditionalDataProvider {
    fun addAdditionalCropBlocks(consumer: (() -> Item) -> Unit) {
        consumer { Items.BAMBOO }
        consumer { Items.SUGAR_CANE }
        consumer { Items.VINE }
        consumer { Items.TWISTING_VINES }
        consumer { Items.WEEPING_VINES }
        consumer { Items.GLOW_BERRIES }
        consumer { Items.KELP }
        consumer { Items.COCOA_BEANS }
    }
}