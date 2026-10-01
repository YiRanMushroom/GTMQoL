package com.yiran.minecraft.gtmqol.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yiran.minecraft.gtmqol.api.NotifiableItemStackCatalystHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemBusPartMachine.class)
public class ItemBusPartMachineMixin {
    @WrapOperation(method = "createCircuitItemHandler", at = @At(value = "NEW", target = "(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;ILcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;"))
    private NotifiableItemStackHandler createCircuitItemHandler(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO, Operation<NotifiableItemStackHandler> original) {
        return new NotifiableItemStackCatalystHandler(machine, slots, handlerIO, capabilityIO);
    }
}
