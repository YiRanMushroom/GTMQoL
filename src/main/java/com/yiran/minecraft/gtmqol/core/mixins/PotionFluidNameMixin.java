package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;

import java.util.Iterator;

/**
 * Potion names don't tell the normal, long and strong variants apart (potion items do it in their tooltip), which
 * fluid slots and tanks don't show. Add the first effect's potency and duration, like the item tooltip does.
 * Splash / lingering potion fluids (see {@link PotionContainers}) are named like their item.
 */
@Mixin(value = PotionFluid.PotionFluidType.class, remap = false)
public class PotionFluidNameMixin {

    public Component getDescription(FluidStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        MutableComponent name = Component.translatable(Potion.getName(contents.potion(),
                PotionContainers.container(stack).getDescriptionId() + ".effect."));
        Iterator<MobEffectInstance> effects = contents.getAllEffects().iterator();
        if (!effects.hasNext()) return name;
        MobEffectInstance effect = effects.next();
        if (effect.getAmplifier() > 0) {
            name = Component.translatable("potion.withAmplifier", name,
                    Component.translatable("potion.potency." + effect.getAmplifier()));
        }
        // instant effects have no duration
        if (!effect.endsWithin(20)) {
            name = Component.translatable("potion.withDuration", name, MobEffectUtil.formatDuration(effect, 1, 20));
        }
        return name;
    }
}
