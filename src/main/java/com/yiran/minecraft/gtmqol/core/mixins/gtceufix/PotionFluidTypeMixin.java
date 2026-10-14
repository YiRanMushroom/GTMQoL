package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;

import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * gtceu 1.21 bug: potion fluids are named with the fluid's own key + {@code .effect.<potion>}, which has no
 * translations, so they showed raw keys. Use the potion item's keys, like gtceu 1.20 does. Remove once gtceu fixes it.
 */
@Mixin(value = PotionFluid.PotionFluidType.class, remap = false)
public class PotionFluidTypeMixin {

    @ModifyArg(method = "getDescriptionId(Lnet/neoforged/neoforge/fluids/FluidStack;)Ljava/lang/String;",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/item/alchemy/Potion;getName(Ljava/util/Optional;Ljava/lang/String;)Ljava/lang/String;"),
               index = 1)
    private String gtmqol$potionItemKey(String prefix) {
        return Items.POTION.getDescriptionId() + ".effect.";
    }
}
