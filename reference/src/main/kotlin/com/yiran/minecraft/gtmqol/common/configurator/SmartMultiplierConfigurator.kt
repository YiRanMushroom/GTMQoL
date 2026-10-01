package com.yiran.minecraft.gtmqol.common.configurator

import appeng.core.definitions.AEItems
import com.extendedae_plus.api.smartDoubling.ISmartDoublingHolder
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget
import com.gregtechceu.gtceu.common.data.GTItems
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack


class SmartMultiplierConfigurator(val smartDoublingHolder: ISmartDoublingHolder) : IFancyConfigurator {

    override fun getTitle(): Component {
        return Component.translatable("gtmqol.configurator.smart_multiplier.title")
    }

    override fun getIcon(): IGuiTexture {
        return ItemStackTexture(GTItems.TOOL_DATA_STICK.asStack());
    }

    override fun createConfigurator(): Widget {
        val group = WidgetGroup(0, 0, 160, 70)

        group.addWidget(LabelWidget(4, 2) {
//            "gtmqol.gui.title.pattern_buffer.toggle_smart_multiplier"
            Component.translatable(
                "gtmqol.gui.title.pattern_buffer.toggle_smart_multiplier",
                if (smartDoublingHolder.`eap$getSmartDoubling`())
                    Component.translatable("gtmqol.gui.yes") else Component.translatable("gtmqol.gui.no")
            ).string
        })
        group.addWidget(
            SwitchWidget(4, 12, 20, 20) { clickedData: ClickData, value: Boolean ->
                smartDoublingHolder.`eap$setSmartDoubling`(value)
            }.setPressed(smartDoublingHolder.`eap$getSmartDoubling`())
                .setTexture(
                    ItemStackTexture(ItemStack(AEItems.BLANK_PATTERN.asItem())),
                    ItemStackTexture(ItemStack(AEItems.PROCESSING_PATTERN.asItem()))
                )
        )
        group.addWidget(LabelWidget(4, 40) {
//            "gtmqol.gui.title.pattern_buffer.set_smart_multiplier_limit"
            Component.translatable(
                "gtmqol.gui.title.pattern_buffer.set_smart_multiplier_limit",
                if (smartDoublingHolder.`eap$getProviderSmartDoublingLimit`() == 0)
                    Component.translatable("gtmqol.dict.infinite").string else smartDoublingHolder.`eap$getProviderSmartDoublingLimit`()
                    .toString()
            ).string
        })
        group.addWidget(IntInputWidget(4, 50, 81, 14, {
            smartDoublingHolder.`eap$getProviderSmartDoublingLimit`()
        }, {
            smartDoublingHolder.`eap$setProviderSmartDoublingLimit`(it)
        }).setMin(0))

        return group
    }
}