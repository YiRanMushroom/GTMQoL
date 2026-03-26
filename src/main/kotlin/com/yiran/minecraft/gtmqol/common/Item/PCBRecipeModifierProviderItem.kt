package com.yiran.minecraft.gtmqol.common.Item

import com.yiran.minecraft.gtmqol.common.multiblocks.PCBFactoryMachine
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.OptionalInt
import kotlin.properties.Delegates

class PCBRecipeModifierProviderItem(properties: Properties) : Item(properties),
    PCBFactoryMachine.IPCBRecipeModifierProvider {

    override fun providePCBRecipeModifier(): PCBFactoryMachine.IPCBRecipeModifier {
        return modifier
    }

    lateinit var modifier: PCBFactoryMachine.IPCBRecipeModifier
    lateinit var outputMultiplier: OptionalInt
    lateinit var durationMultiplier: OptionalInt

    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag
    ) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
        tooltipComponents.add(Component.translatable("item.gtmqol.pcb_recipe_modifier_provider.tooltip",
            this.outputMultiplier.asInt, this.durationMultiplier.asInt).withStyle(Style.EMPTY.withColor(0x00FF00)))
    }

    companion object {
        @JvmStatic
        fun factory(outputMultiplier: Int, durationMultiplier: Int): (Properties) -> Item {
            return { properties ->
                val item = PCBRecipeModifierProviderItem(properties)
                item.modifier =
                    PCBFactoryMachine.IPCBRecipeModifier.simpleModifier(outputMultiplier, durationMultiplier)

                item.outputMultiplier = OptionalInt.of(outputMultiplier)
                item.durationMultiplier = OptionalInt.of(durationMultiplier)

                item
            }
        }
    }
}