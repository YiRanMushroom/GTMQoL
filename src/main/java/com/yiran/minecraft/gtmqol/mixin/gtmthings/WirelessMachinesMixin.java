package com.yiran.minecraft.gtmqol.mixin.gtmthings;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.WirelessEnergyHatchPartMachine;
import com.hepdd.gtmthings.common.registry.GTMTRegistration;
import com.hepdd.gtmthings.data.WirelessMachines;
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate;
import com.yiran.minecraft.gtmqol.config.ConfigHolder;
import com.yiran.minecraft.gtmqol.gtmthings.WirelessEnergyAccessor;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Condition(
        value = "gtmthings",
        type = Condition.Type.MOD
)
@Mixin(WirelessMachines.class)
public class WirelessMachinesMixin {
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onStaticInit(CallbackInfo ci) {
        if (ConfigHolder.getInstance().addonConfig.enableGTMThingsIntegration) {
            WirelessEnergyAccessor.INSTANCE = GTMTRegistration.GTMTHINGS_REGISTRATE.machine("wireless_energy_accessor", WirelessEnergyAccessor::new)
                    .rotationState(RotationState.ALL).overlayTieredHullModel("energy_output_hatch").tier(5).register();

            final MachineDefinition[] l_WIRELESS_ENERGY_INPUT_HATCH_256A = qol$registerWirelessLaserHatch(IO.IN, 256, PartAbility.INPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_INPUT_HATCH_1024A = qol$registerWirelessLaserHatch(IO.IN, 1024, PartAbility.INPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_INPUT_HATCH_4096A = qol$registerWirelessLaserHatch(IO.IN, 4096, PartAbility.INPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_INPUT_HATCH_16384A = qol$registerWirelessLaserHatch(IO.IN, 16384, PartAbility.INPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_INPUT_HATCH_65536A = qol$registerWirelessLaserHatch(IO.IN, 65536, PartAbility.INPUT_LASER, GTValues.tiersBetween(ULV, HV));

            final MachineDefinition[] l_WIRELESS_ENERGY_OUTPUT_HATCH_256A = qol$registerWirelessLaserHatch(IO.OUT, 256, PartAbility.OUTPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_OUTPUT_HATCH_1024A = qol$registerWirelessLaserHatch(IO.OUT, 1024, PartAbility.OUTPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_OUTPUT_HATCH_4096A = qol$registerWirelessLaserHatch(IO.OUT, 4096, PartAbility.OUTPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_OUTPUT_HATCH_16384A = qol$registerWirelessLaserHatch(IO.OUT, 16384, PartAbility.OUTPUT_LASER, GTValues.tiersBetween(ULV, HV));
            final MachineDefinition[] l_WIRELESS_ENERGY_OUTPUT_HATCH_65536A = qol$registerWirelessLaserHatch(IO.OUT, 65536, PartAbility.OUTPUT_LASER, GTValues.tiersBetween(ULV, HV));

            for (int i = ULV; i <= HV; ++i) {
                WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_256A[i] = l_WIRELESS_ENERGY_INPUT_HATCH_256A[i];
                WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_1024A[i] = l_WIRELESS_ENERGY_INPUT_HATCH_1024A[i];
                WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4096A[i] = l_WIRELESS_ENERGY_INPUT_HATCH_4096A[i];
                WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16384A[i] = l_WIRELESS_ENERGY_INPUT_HATCH_16384A[i];
                WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_65536A[i] = l_WIRELESS_ENERGY_INPUT_HATCH_65536A[i];

                WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_256A[i] = l_WIRELESS_ENERGY_OUTPUT_HATCH_256A[i];
                WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_1024A[i] = l_WIRELESS_ENERGY_OUTPUT_HATCH_1024A[i];
                WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4096A[i] = l_WIRELESS_ENERGY_OUTPUT_HATCH_4096A[i];
                WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16384A[i] = l_WIRELESS_ENERGY_OUTPUT_HATCH_16384A[i];
                WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_65536A[i] = l_WIRELESS_ENERGY_OUTPUT_HATCH_65536A[i];
            }
        }

    }

    @Shadow
    private static String getRender(int amperage) {
        String render = "wireless_energy_hatch";
        String var10000;
        switch (amperage) {
            case 2 -> var10000 = render;
            case 4 -> var10000 = render + "_4a";
            case 16 -> var10000 = render + "_16a";
            case 64 -> var10000 = render + "_64a";
            default -> var10000 = "wireless_laser_hatch";
        }

        render = var10000;
        return render;
    }

    @Unique
    private static MachineDefinition[] qol$registerWirelessLaserHatch(IO io, int amperage, PartAbility ability, int[] tiers) {
        var name = io == IO.IN ? "target" : "source";
        String finalRender = getRender(amperage);
        return qol$egisterTieredMachines(amperage + "a_wireless_laser_" + name + "_hatch",
                (holder, tier) -> new WirelessEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + " " + FormattingUtil.formatNumbers(amperage) + "A Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .tooltips(Component.translatable("gtmthings.machine.energy_hatch." + name + ".tooltip"), (Component.translatable("gtmthings.machine.wireless_energy_hatch." + name + ".tooltip")))
                        .overlayTieredHullModel(finalRender)
                        .register(),
                tiers);
    }

    private static MachineDefinition[] qol$egisterTieredMachines(String name,
                                                                 BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                 BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                                 int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = GTMQoLRegistrate.getREGISTRATE()
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }
}
