package com.yiran.minecraft.gtmqol.mixin;

import appeng.api.inventories.InternalInventory;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.yiran.minecraft.gtmqol.integration.ae2.AbstractMEPatternBufferPartMachine;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MEPatternBufferPartMachine.class)
public abstract class MEPatternBufferPartMachineInternalInventoryAbstractionMixin {
    @Final
    @Mutable
    @Shadow
    private InternalInventory internalPatternInventory;

    @Shadow
    protected abstract void onPatternChange(int index);

    @Shadow
    @Final
    private CustomItemStackHandler patternInventory;

    @Definition(id = "internalPatternInventory", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;internalPatternInventory:Lappeng/api/inventories/InternalInventory;")
    @Expression("this.internalPatternInventory = ?")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void initInternalPatternInventory(MEPatternBufferPartMachine instance, InternalInventory value, Operation<Void> original) {
        internalPatternInventory = new InternalInventory() {

            @Override
            public int size() {
                if ((Object) MEPatternBufferPartMachineInternalInventoryAbstractionMixin.this instanceof AbstractMEPatternBufferPartMachine apbm) {
                    return apbm.getMaxPatternCount();
                }

                return 27;
            }

            @Override
            public ItemStack getStackInSlot(int slotIndex) {
                return MEPatternBufferPartMachineInternalInventoryAbstractionMixin.this.patternInventory.getStackInSlot(slotIndex);
            }

            @Override
            public void setItemDirect(int slotIndex, ItemStack stack) {
                MEPatternBufferPartMachineInternalInventoryAbstractionMixin.this.patternInventory.setStackInSlot(slotIndex, stack);
                MEPatternBufferPartMachineInternalInventoryAbstractionMixin.this.patternInventory.onContentsChanged(slotIndex);
                MEPatternBufferPartMachineInternalInventoryAbstractionMixin.this.onPatternChange(slotIndex);
            }
        };
    }

    @Definition(id = "group", local = @Local(type = WidgetGroup.class, name = "group"))
    @Expression("group = @(?)")
    @Inject(method = "createUIWidget", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.BEFORE))
    private void captureGroup(CallbackInfoReturnable<Widget> cir, @Local(name = "rowSize") LocalIntRef rowSize, @Local(name = "colSize") LocalIntRef colSize) {
        if (((Object) this) instanceof AbstractMEPatternBufferPartMachine apbm) {
            rowSize.set(apbm.getPatternGridSize().secondInt());
            colSize.set(apbm.getPatternGridSize().firstInt());
        }
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 27))
    private int modifyPatternInventorySize(int original) {
        if (((Object) this) instanceof AbstractMEPatternBufferPartMachine apbm) {
            return apbm.getMaxPatternCount();
        }
        return original;
    }

    @Definition(id = "shareInventory", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;shareInventory:Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;")
    @Expression("this.shareInventory = @(?)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private NotifiableItemStackHandler initSharedInventory(MetaMachine machine, int slots, IO handlerIO, IO capabilityIO, Operation<NotifiableItemStackHandler> original) {
        if (!(machine instanceof AbstractMEPatternBufferPartMachine apbm)) {
            return original.call(machine, slots, handlerIO, capabilityIO);
        }

        return apbm.createSharedItemStackHandler();
    }

    @Definition(id = "shareTank", field = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEPatternBufferPartMachine;shareTank:Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;")
    @Expression("this.shareTank = @(?)")
    @WrapOperation(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private NotifiableFluidTank initSharedTank(MetaMachine machine, int slots, int capacity, IO io, IO capabilityIO, Operation<NotifiableFluidTank> original) {
        if (!(machine instanceof AbstractMEPatternBufferPartMachine apbm)) {
            return original.call(machine, slots, capacity, io, capabilityIO);
        }

        return apbm.createSharedFluidTank();
    }
}
