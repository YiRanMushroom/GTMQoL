package com.yiran.minecraft.gtmqol.mixin;

import appeng.api.inventories.InternalInventory;
import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MEPatternBufferPartMachine.class)
public interface MEPatternBufferPartMachineAccessor {
//    @Accessor("internalPatternInventory")
//    public InternalInventory qol$getInternalPatternInventory();
//
//    @Invoker("refundAll")
//    public void refundAll(ClickData clickData);
//
//    @Invoker("onPatternChange")
//    public void onPatternChange(int index);
//
//    @Accessor("customName")
//    public String qol$getCustomName();
}
