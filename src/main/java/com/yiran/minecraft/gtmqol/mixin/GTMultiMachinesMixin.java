package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fusion reactor energy slots ('E') also accept substation and laser hatches of the same tier range.
 * <p>
 * The call is inside the pattern lambda nested in the {@code registerTieredMultis} builder lambda, whose
 * synthetic name depends on javac's numbering. The regex selector matches every lambda in the class; only the
 * fusion one calls {@code getBlockRange}. If this misses, replace it with the exact name from {@code javap -p}.
 */
@Mixin(value = GTMultiMachines.class, remap = false)
public class GTMultiMachinesMixin {

    @WrapOperation(method = "/^lambda\\$/",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;getBlockRange(II)Ljava/util/Collection;"))
    private static Collection<Block> gtmqol$fusionEnergyHatches(PartAbility ability, int from, int to,
                                                                Operation<Collection<Block>> original) {
        Collection<Block> blocks = original.call(ability, from, to);
        if (ability != PartAbility.INPUT_ENERGY || GTMQoLConfig.INSTANCE == null ||
                !GTMQoLConfig.INSTANCE.overclocking.buffFusionReactor) {
            return blocks;
        }
        List<Block> all = new ArrayList<>(blocks);
        all.addAll(PartAbility.SUBSTATION_INPUT_ENERGY.getBlockRange(from, to));
        all.addAll(PartAbility.INPUT_LASER.getBlockRange(from, to));
        return all;
    }

    /** The steam grinder and steam oven keep all of a recipe's outputs, see {@code GTMachinesMixin}. */
    @SuppressWarnings("rawtypes")
    @WrapOperation(method = "<clinit>",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MultiblockMachineBuilder;addOutputLimit(Lcom/gregtechceu/gtceu/api/capability/recipe/RecipeCapability;I)Lcom/gregtechceu/gtceu/api/registry/registrate/builder/MachineBuilder;"),
                   require = 2)
    private static MachineBuilder gtmqol$noOutputLimit(MultiblockMachineBuilder builder, RecipeCapability<?> cap,
                                                       int limit, Operation<MachineBuilder> original) {
        return builder;
    }
}
