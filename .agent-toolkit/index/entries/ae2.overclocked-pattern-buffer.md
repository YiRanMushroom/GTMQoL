+++
title = "Overclocked ME pattern buffer (12x18 = 216 slots, LuV): resizing GTCEu's pattern buffer"
keys = [ "27", "max_pattern_count", "overclocked_me_pattern_buffer" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AbstractMEPatternBufferPartMachine.java"
snippet = "public abstract class AbstractMEPatternBufferPartMachine extends MEPatternBufferPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MEPatternBufferPartMachineMixin.java"
snippet = "    @ModifyConstant(method = { \"<init>\", \"syncWorkerCount\", \"addWorker\" },\n                    constant = @Constant(intValue = 27),\n                    require = 5)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AE2Machines.java"
snippet = "                .machine(\"overclocked_me_pattern_buffer\", info -> new AbstractMEPatternBufferPartMachine(info) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AE2Machines.java"
snippet = "    public static void addRecipes(RecipeOutput provider) {"
+++
require = 5 on the @ModifyConstant: recheck the count of inlined 27s after a GTCEu bump. The shared inventory/tank stay at GTCEu's 9 slots (the old 25-slot catalyst version would need all of getPanelBuilder). Recipe (magical assembler) matches the old v7 QoLMachines.kt. AE2Machines also registers the lang for smart doubling, output marks and shared hatches toggles.