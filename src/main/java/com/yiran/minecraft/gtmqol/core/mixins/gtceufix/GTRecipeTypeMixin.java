package com.yiran.minecraft.gtmqol.core.mixins.gtceufix;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * gtceu 1.21 bug: {@code GTRecipeTypeBuilder.addCustomRecipeLogic} only adds to {@code Properties}, and the
 * constructor never copies that list, so no recipe type runs its custom logic (forming press naming, macerator/arc
 * furnace rotor recycling, canner, brewery, data stick copying) or shows its representative recipes. Copy it here.
 * Skipped if the list is already filled, so an upstream fix doesn't run the logic twice. Remove once gtceu fixes it.
 */
@Mixin(value = GTRecipeType.class, remap = false)
public class GTRecipeTypeMixin {

    @Shadow
    @Final
    private List<GTRecipeType.ICustomRecipeLogic> customRecipeLogicRunners;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtmqol$copyCustomRecipeLogic(ResourceLocation id, GTRecipeType.Properties properties,
                                              CallbackInfo ci) {
        if (customRecipeLogicRunners.isEmpty()) {
            customRecipeLogicRunners.addAll(properties.customRecipeLogicRunners());
        }
    }
}
