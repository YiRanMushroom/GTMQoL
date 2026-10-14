package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStackSimple;
import com.gregtechceu.gtceu.common.fluid.potion.BottleItemFluidHandler;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A glass bottle filled with a splash / lingering potion fluid becomes that potion (see {@link PotionContainers}). */
@Mixin(value = BottleItemFluidHandler.class, remap = false)
public abstract class BottleItemFluidHandlerMixin extends FilteredFluidHandlerItemStackSimple {

    private BottleItemFluidHandlerMixin() {
        super(null, null, 0, null);
    }

    @Inject(method = "setFluid", at = @At("TAIL"))
    private void gtmqol$container(FluidStack fluid, CallbackInfo ci) {
        if (fluid.isEmpty()) return;
        Item item = PotionContainers.container(fluid);
        if (item != Items.POTION) container = container.transmuteCopy(item, container.getCount());
    }
}
