package com.yiran.minecraft.mixin;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.forge.ForgeCommonEventListener;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.yiran.minecraft.gtmqol.fe_to_eu_conversion.FEToEUProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ForgeCommonEventListener.class)
public class ForgeCommonEventListener$AttachEnergyProxy$Mixin {
    @WrapMethod(method = "registerBlockEntityCapabilities")
    private static void checkAndAttachFEToEUCapability(AttachCapabilitiesEvent<BlockEntity> event, Operation<Void> original) {
        if (event.getObject() instanceof IMachineBlockEntity) {
            event.addCapability(GTCEu.id("eu_capability"), new FEToEUProvider(event.getObject()));
        } else {
            original.call(event);
        }
    }
}
