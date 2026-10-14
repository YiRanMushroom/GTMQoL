package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.api.RecipeModifierPartMachines;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.stream.Stream;

@Mixin(GTMultiMachines.class)
public class GTMultiMachines$Misc$Mixin {
    @WrapOperation(method = "lambda$static$53", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;getBlockRange(II)Ljava/util/Collection;"))
    private static Collection<Block> FusionUseAllEnergyHatches(PartAbility instance, int from, int to, Operation<Collection<Block>> original) {
        if (ConfigHolder.getInstance().overclockingConfig.buffFusionReactorOverclocking) {
            return Stream.concat(Stream.concat(original.call(instance, from, to).stream(),
                            PartAbility.SUBSTATION_INPUT_ENERGY.getBlockRange(from, to).stream()),
                    PartAbility.INPUT_LASER.getBlockRange(from, to).stream()).toList();
        } else {
            return original.call(instance, from, to);
        }
    }

    @Definition(id = "where", method = "Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;where(CLcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;")
    @Expression("?.where('Z', @(?))")
    @ModifyExpressionValue(method = "lambda$static$33", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TraceabilityPredicate addModifierForDistillationTower(TraceabilityPredicate original) {
        return original.or(Predicates.abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
    }

    @Definition(id = "where", method = "Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;where(CLcom/gregtechceu/gtceu/api/pattern/TraceabilityPredicate;)Lcom/gregtechceu/gtceu/api/pattern/FactoryBlockPattern;")
    @Expression("?.where('Y', @(?))")
    @ModifyExpressionValue(method = "lambda$static$41", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TraceabilityPredicate addModifierForAssemblyLine(TraceabilityPredicate original) {
        return original.or(Predicates.abilities(RecipeModifierPartMachines.QOL_RECIPE_MODIFIER));
    }
}
