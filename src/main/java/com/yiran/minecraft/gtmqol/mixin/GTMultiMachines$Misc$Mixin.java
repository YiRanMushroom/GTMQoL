package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
        if (ConfigHolder.getInstance().overlockingConfig.buffFusionReactorOverclocking) {
            return Stream.concat(Stream.concat(original.call(instance, from, to).stream(),
                            PartAbility.SUBSTATION_INPUT_ENERGY.getBlockRange(from, to).stream()),
                    PartAbility.INPUT_LASER.getBlockRange(from, to).stream()).toList();
        } else {
            return original.call(instance, from, to);
        }
    }
}
