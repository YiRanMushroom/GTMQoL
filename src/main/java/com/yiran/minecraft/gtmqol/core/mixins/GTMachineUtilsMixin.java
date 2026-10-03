package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Every tiered single-block machine (gtceu's, ours, other addons') goes through this. */
@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    // The definitions don't exist yet on 1.21 (they are created in the machine registry event), so the
    // recipe types are taken from the first tier's builder instead.
    @WrapOperation(method = "registerTieredMachines",
                   at = @At(value = "INVOKE",
                            target = "Ljava/util/function/BiFunction;apply(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object gtmqol$captureRecipeTypes(BiFunction<?, ?, ?> function, Object tier, Object builder,
                                                    Operation<Object> original,
                                                    @Share("recipeTypes") LocalRef<Set<Supplier<GTRecipeType>>> recipeTypes) {
        if (recipeTypes.get() == null) {
            recipeTypes.set(((MachineBuilderAccessor) builder).gtmqol$getUnresolvedRecipeTypes());
        }
        return original.call(function, tier, builder);
    }

    // Large boilers: the muffler and the maintenance hatch (the only two setExactLimit calls in the pattern lambda)
    // become optional, so the large boilers can be built before LV.
    @WrapOperation(method = "/^lambda\\$registerLargeBoiler\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;setExactLimit(I)Lcom/gregtechceu/gtceu/api/multiblock/MultiPredicate;"),
                   require = 2)
    private static MultiPredicate gtmqol$optionalBoilerParts(MultiPredicate predicate, int limit,
                                                             Operation<MultiPredicate> original) {
        if (!GTMQoLConfig.get().steamTweaks.optionalLargeBoilerParts) return original.call(predicate, limit);
        return predicate.setMaxGlobalLimited(limit);
    }

    @Inject(method = "registerTieredMachines", at = @At("RETURN"))
    private static void gtmqol$registerModular(GTRegistrate registrate, String name,
                                               MachineInstanceFactory.Tiered<?> factory,
                                               BiFunction<Integer, MachineBuilder<MachineDefinition, ?, ?>, MachineEntry<MachineDefinition>> builder,
                                               int[] tiers, CallbackInfoReturnable<MachineEntry<MachineDefinition>[]> cir,
                                               @Share("recipeTypes") LocalRef<Set<Supplier<GTRecipeType>>> recipeTypes) {
        if (recipeTypes.get() != null) {
            ModularMachines.queue(registrate, name, cir.getReturnValue(), recipeTypes.get());
        }
    }
}
