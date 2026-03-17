package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMachines;

@Mixin(GTMachineUtils.class)
public class GTMachineUtils$Lasers$Mixin {
//    @ModifyExpressionValue(method = "registerLaserHatch(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;ILcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;", at = @At(value = "FIELD", target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;HIGH_TIERS:[I"))
//    private static int[] modifyHighTiers(int[] original) {
//        return ModUtils.getLaserTiers();
//    }

    @Shadow
    @Final
    public static int[] LOW_TIERS;

    @WrapMethod(method = "registerLaserHatch(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;ILcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;)[Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;")
    private static MachineDefinition[] modifyHighTiers(GTRegistrate registrate, IO io, int amperage, PartAbility ability, Operation<MachineDefinition[]> original) {
        var HighTierDefinitions = original.call(registrate, io, amperage, ability);

        if ((registrate != GTRegistration.REGISTRATE && registrate != GTMQoLRegistrate.getREGISTRATE()) || !ConfigHolder.getInstance().addonConfig.registerLaserHatchesForAllTiers) {
            return HighTierDefinitions;
        }

        String name = io == IO.IN ? "target" : "source";
        var LowDefinition = registerTieredMachines(GTMQoLRegistrate.getREGISTRATE(), amperage + "a_laser_" + name + "_hatch", (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> {
            String var10001 = GTValues.VNF[tier];
            return builder.langValue(var10001 + "§r " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " + FormattingUtil.toEnglishName(name) + " Hatch").rotationState(RotationState.ALL).tooltips(new Component[]{Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"), Component.translatable("gtceu.machine.laser_hatch.both.tooltip"), Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IO.IN ? "in" : "out"), new Object[]{FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]}), Component.translatable("gtceu.universal.tooltip.amperage_in", new Object[]{amperage}), Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", new Object[]{FormattingUtil.formatNumbers(EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))}), Component.translatable("gtceu.part_sharing.disabled")}).abilities(new PartAbility[]{ability}).modelProperty(GTMachineModelProperties.IS_FORMED, false).overlayTieredHullModel("laser_" + name + "_hatch").register();
        }, LOW_TIERS);

        for (int tier : LOW_TIERS) {
            HighTierDefinitions[tier] = LowDefinition[tier];
        }

        return HighTierDefinitions;
    }

}
