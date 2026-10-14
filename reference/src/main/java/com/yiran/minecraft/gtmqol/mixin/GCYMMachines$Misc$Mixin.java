package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.api.RecipeModifierPartMachines;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GCYMMachines.class)
public class GCYMMachines$Misc$Mixin {
    @Definition(id = "where", method = "Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;where(CLcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;")
    @Expression("?.where('Y', @(?))")
    @ModifyExpressionValue(method = "lambda$static$40", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TraceabilityPredicate addModifierForLargeDistillery(TraceabilityPredicate original) {
        original.or(Predicates.abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
        return original;
    }

    @Definition(id = "recipeType", method = "Lcom/gregtechceu/gtceu/api/registry/registrate/MultiblockMachineBuilder;recipeType(Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;)Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;")
    @Definition(id = "BLAST_RECIPES", field = "Lcom/gregtechceu/gtceu/common/data/GTRecipeTypes;BLAST_RECIPES:Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;")
    @Expression("?.recipeType(BLAST_RECIPES)")
    @WrapOperation(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static MachineBuilder addModifierForBlastRecipes(MultiblockMachineBuilder instance, GTRecipeType gtRecipeType, Operation<MachineBuilder> original) {
        return instance.recipeTypes(GTRecipeTypes.BLAST_RECIPES, GCYMRecipeTypes.ALLOY_BLAST_RECIPES);
    }

    @Definition(id = "translatableS", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "translatableSO", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;")
    @Definition(id = "tooltips", method = "Lcom/gregtechceu/gtceu/api/registry/registrate/MultiblockMachineBuilder;tooltips([Lnet/minecraft/network/chat/Component;)Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;")
    @Definition(id = "Component", type = Component.class)
    @Definition(id = "Object", type = Object.class)
    @Expression("?.tooltips(new Component[]{translatableSO('gtceu.machine.available_recipe_map_1.tooltip', new Object[]{translatableS('gtceu.electric_blast_furnace')})})")
    @WrapOperation(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static MachineBuilder addTooltipForBlastRecipes(MultiblockMachineBuilder instance, Component[] components, Operation<MachineBuilder> original) {
        return instance.tooltips(Component.translatable("gtceu.machine.available_recipe_map_2.tooltip",
                Component.translatable("gtceu.electric_blast_furnace"), Component.translatable("gtceu.alloy_blast_smelter")));
    }

    @Definition(id = "where", method = "Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;where(CLcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;")
    @Expression("?.where('Y', @(?))")
    @ModifyExpressionValue(method = "lambda$static$40", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TraceabilityPredicate addModifierForAssemblyLine(TraceabilityPredicate original) {
        return original.or(Predicates.abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
    }
}
