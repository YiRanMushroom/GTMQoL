+++
title = "Mark pattern outputs: each pattern buffer slot is a ghost ingredient mark for its pattern's primary output"
keys = [ "handlersfor", "markspatternoutputs", "recipe lookup" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/PatternOutputMarks.java"
snippet = "public final class PatternOutputMarks implements IRecipeHandler<GhostIngredient<?>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferOutputMarksMixin.java"
snippet = "    @Inject(method = \"handlersFor\", at = @At(\"HEAD\"), cancellable = true)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AbstractMEPatternBufferPartMachine.java"
snippet = "        getBufferRecipeHandler().addHandlers(new PatternOutputMarks(this::allOutputMarks));"
+++
Only on our overclocked buffer (ae2.overclocked-pattern-buffer), default off, toggle uses PATTERN_OVERLAY. The aggregate of all slots' marks is only for the recipe lookup; while matching, handlersFor answers with the worker's own pattern mark (via MEPatternBufferWorkerPatternAccessor). Proxies don't get the aggregate, so it doesn't work through them.