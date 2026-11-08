package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.integration.recipeviewer.jei.subtype.PotionFluidSubtypeInterpreter;
import com.yiran.minecraft.gtmqol.common.potion.PotionContainers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mezz.jei.api.ingredients.subtypes.UidContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * JEI tells potion fluids apart by their container too (plain, splash, lingering, see {@link PotionContainers}), so
 * looking one up doesn't find the others' recipes. JEI takes the first subtype interpreter registered for a fluid,
 * which is GTCEu's, so it is extended here rather than replaced by our own plugin. EMI: PotionEmi.
 */
// Untested: with JEI (no EMI), recipes for a splash potion fluid don't list the plain or lingering one's
@Mixin(value = PotionFluidSubtypeInterpreter.class, remap = false)
public class PotionFluidSubtypeInterpreterMixin {

    @ModifyReturnValue(method = "apply(Lnet/minecraftforge/fluids/FluidStack;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/String;",
                       at = @At("RETURN"))
    private String gtmqol$withContainer(String subtype, FluidStack fluid, UidContext context) {
        var container = PotionContainers.container(fluid);
        return container == Items.POTION ? subtype : subtype + ";" + BuiltInRegistries.ITEM.getKey(container);
    }
}
