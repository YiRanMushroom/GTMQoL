+++
title = "GT's ME output hatch buffers past Integer.MAX_VALUE per fluid (MEOutputHatchBufferMixin)"
kind = "code"
keys = [ "2.1g", "int max", "internalbuffer", "me output hatch", "output buffer" ]
updated = "2026-11-10 07:54:29"
checked = "2026-11-10"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEOutputHatchBufferMixin.java"
snippet = "private void gtmqol$fillPastIntMax(FluidStack resource, FluidAction action, CallbackInfoReturnable<Integer> cir) {"
+++
User's request 2026-11-10. gtceu's FluidStorageDelegate.fill saturated the buffered amount to int and took at most Integer.MAX_VALUE minus it; the ME output bus and our MEDualOutputPartMachine.BufferStorage already used longs. Only the buffer: GT's output parallel cap (ItemRecipeCapability/FluidRecipeCapability.limitMaxParallelByOutput: multiplier <= Integer.MAX_VALUE / largest output count) stays, since a recipe's output amount is an int; lifting it would need outputs split into several entries.