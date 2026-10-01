package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GTRecipeModifiers.class)
public class GTRecipeModifiers$FixMultiSmelterForOC$Mixin {
    @Definition(id = "andThen", method = "Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;andThen(Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;")
    @Expression("return @(?.andThen(?))")
    @ModifyExpressionValue(method = "multiSmelterParallel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ModifierFunction multiSmelterParallelReturn(ModifierFunction original,
                                                               @Local(name = "machine") MetaMachine machine,
                                                               @Local(name = "coilMachine") CoilWorkableElectricMultiblockMachine coilMachine,
                                                               @Local(name = "recipe") GTRecipe recipe) {
        return original.andThen(
                OverclockingLogic.NON_PERFECT_OVERCLOCK.getModifier(machine, original.apply(recipe), coilMachine.getOverclockVoltage())
        );
    }

    @Definition(id = "getModifier", method = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;getModifier(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;J)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;")
    @Definition(id = "NON_PERFECT_OVERCLOCK", field = "Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;NON_PERFECT_OVERCLOCK:Lcom/gregtechceu/gtceu/api/recipe/OverclockingLogic;")
    @Expression("NON_PERFECT_OVERCLOCK.getModifier(?,?,?)")
    @Redirect(method = "multiSmelterParallel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ModifierFunction multiSmelterParallelGetModifier(OverclockingLogic logic, MetaMachine machine, GTRecipe recipe, long maxVoltage) {
        return null;
    }

    @Expression("return @(?.andThen(?)).andThen(?)")
    @Definition(id = "andThen", method = "Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;andThen(Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;)Lcom/gregtechceu/gtceu/api/recipe/modifier/ModifierFunction;")
    @WrapOperation(method = "multiSmelterParallel", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static ModifierFunction multiSmelterParallelBaseModifier(ModifierFunction instance, ModifierFunction after, Operation<ModifierFunction> original) {
        return instance;
    }


}
