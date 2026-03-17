package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate;
import com.yiran.minecraft.gtmqol.ModUtils;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.data.QoLMachines;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(GTMachines.class)
public class GTMachines$AddMachineDefinition$Mixin {
    @Unique
    private static HashMap<Integer, Pair<MachineDefinition[], MachineDefinition[]>> HIGH_AMP_LASERS = new HashMap<>();

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addMachineDefinition(CallbackInfo ci) {
        if (ConfigHolder.getInstance().addonConfig.enableHigherAmpLaserHatches) {
            for (int highLaserAmp : ModUtils.getHighLaserAmps()) {
                HIGH_AMP_LASERS.put(highLaserAmp, new Pair<>(GTMachineUtils.registerLaserHatch(GTMQoLRegistrate.getREGISTRATE(), IO.IN, highLaserAmp, PartAbility.INPUT_LASER), GTMachineUtils.registerLaserHatch(GTMQoLRegistrate.getREGISTRATE(), IO.OUT, highLaserAmp, PartAbility.OUTPUT_LASER)));
            }
        }

        QoLMachines.HIGH_AMP_LASERS = HIGH_AMP_LASERS;
    }
}
