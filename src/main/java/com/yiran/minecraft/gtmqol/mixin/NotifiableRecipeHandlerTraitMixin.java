package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.yiran.minecraft.gtmqol.api.ISlotHint;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NotifiableRecipeHandlerTrait.class)
public class NotifiableRecipeHandlerTraitMixin implements ISlotHint {
    private Object qol$matchingGroup = this;

    @Override
    public Object qol$getMatchingGroup() {
        return qol$matchingGroup;
    }

    @Override
    public void qol$setMatchingGroup(Object group) {
        this.qol$matchingGroup = group;
    }
}
