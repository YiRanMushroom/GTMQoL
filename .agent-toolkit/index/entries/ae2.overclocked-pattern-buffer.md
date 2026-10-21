+++
title = "Overclocked ME pattern buffer (12x18 = 216 slots, LuV): resizing GTCEu's pattern buffer"
keys = [ "27", "ae2.overclockedpatternbuffer", "max_pattern_count", "overclocked_me_pattern_buffer" ]
updated = "2026-10-19 20:12:36"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MEPatternBufferPartMachineMixin.java"
snippet = "    @ModifyConstant(method = { \"<init>\", \"syncWorkerCount\", \"addWorker\" },\n                    constant = @Constant(intValue = 27),\n                    require = 5)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AE2Machines.java"
snippet = "                .machine(\"overclocked_me_pattern_buffer\", info -> new AbstractMEPatternBufferPartMachine(info) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AE2Machines.java"
snippet = "    public static void addRecipes(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AbstractMEPatternBufferPartMachine.java"
snippet = "public abstract class AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine {"

[[items]]
name = "unformed-terminal-icon"
value = "Unformed, the pattern terminal shows GTCEu's buffer icon for ours (getTerminalGroup, and customName is private), so not overridden."

[[items]]
name = "known-jvm-crash"
value = "Seen once, ignored for now: a JVM access violation (C2 JIT, in InventoryChangeTrigger) while picking up a buffer."
+++
require = 5 on the @ModifyConstant: recheck the count of inlined 27s after a GTCEu bump. The mixin lives in gtmqol.mixins.json (not the AE2 config). Shared inventory/tank stay at GTCEu's 9 slots. Recipe (magical assembler: 4 pattern buffers, 16 MV circuits, circuit 24) matches the old v7 QoLMachines.kt. AE2Machines.init also registers the lang for smart doubling, output marks and shared hatches toggles. Config ae2.overclockedPatternBuffer.