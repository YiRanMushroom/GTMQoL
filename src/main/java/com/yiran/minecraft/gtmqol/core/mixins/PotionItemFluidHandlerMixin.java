package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.common.fluid.potion.PotionItemFluidHandler;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Splash / lingering potions drain into the matching marked fluid, not a plain potion (see {@link PotionContainers}). */
@Mixin(value = PotionItemFluidHandler.class, remap = false)
public class PotionItemFluidHandlerMixin {

    @Inject(method = "getFluid", at = @At("RETURN"), cancellable = true)
    private void gtmqol$container(CallbackInfoReturnable<FluidStack> cir) {
        FluidStack fluid = cir.getReturnValue();
        if (!PotionContainers.isPotion(fluid)) return;
        ItemStack container = ((IFluidHandlerItem) this).getContainer();
        cir.setReturnValue(PotionContainers.withContainer(fluid, container.getItem()));
    }
}
