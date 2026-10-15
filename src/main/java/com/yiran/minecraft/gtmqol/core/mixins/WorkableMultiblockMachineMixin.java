package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.trait.LatheRecipeLogic;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachine;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMultiMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.Arrays;

/**
 * Our multiblocks that run lathe recipes (large steam lathe, modular lathe) get gtceu's {@link LatheRecipeLogic}
 * (25 mB lubricant: 2 rods instead of a rod and dust), like gtceu's lathe and large cutter. They can't pass it in:
 * {@code SteamParallelMultiblockMachine} has no constructor taking a recipe logic, so the plain one is swapped when
 * this constructor attaches it.
 */
@Mixin(value = WorkableMultiblockMachine.class, remap = false)
public abstract class WorkableMultiblockMachineMixin {

    @WrapOperation(method = "<init>(Lcom/gregtechceu/gtceu/api/blockentity/BlockEntityCreationInfo;Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/multiblock/WorkableMultiblockMachine;attachTrait(Lcom/gregtechceu/gtceu/api/machine/trait/MachineTrait;)Lcom/gregtechceu/gtceu/api/machine/trait/MachineTrait;"))
    private MachineTrait gtmqol$latheRecipeLogic(WorkableMultiblockMachine self, MachineTrait trait,
                                                 Operation<MachineTrait> original) {
        if (trait.getClass() == RecipeLogic.class &&
                (self instanceof AdvancedSteamMultiMachine || self instanceof ModularMachine) &&
                Arrays.asList(self.getDefinition().getRecipeTypes()).contains(GTRecipeTypes.LATHE_RECIPES.get())) {
            trait = new LatheRecipeLogic();
        }
        return original.call(self, trait);
    }
}
