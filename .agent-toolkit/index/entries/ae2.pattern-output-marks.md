+++
title = "Mark pattern outputs: each pattern buffer slot is a ghost ingredient mark for its pattern's primary output"
keys = [ "handlersfor", "markspatternoutputs", "recipe lookup" ]
updated = "2026-11-11 08:18:48"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/patternbuffer/PatternOutputMarks.java"
snippet = "public final class PatternOutputMarks implements IRecipeHandler<GhostIngredient<?>> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferOutputMarksMixin.java"
snippet = "    @Inject(method = \"handlersFor\", at = @At(\"HEAD\"), cancellable = true)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/patternbuffer/AbstractMEPatternBufferPartMachine.java"
snippet = "        getBufferRecipeHandler().addHandlers(new PatternOutputMarks(this::allOutputMarks));"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferProxyStackLikeMixin.java"
snippet = "((RecipeHandlerList) (Object) this).addHandlers(new PatternOutputMarks(this::gtmqol$outputMarks));"
+++
Only on our overclocked buffer (ae2.overclocked-pattern-buffer), default off, toggle uses PATTERN_OVERLAY. The aggregate of all slots' marks is only for the recipe lookup; while matching, handlersFor answers with the worker's own pattern mark (via MEPatternBufferWorkerPatternAccessor). An ME Pattern Buffer Proxy works the same way (2026-11-11): its handler list (ProxyRHL) gets a PatternOutputMarks over the buffer's aggregate for the lookup (MEPatternBufferProxyStackLikeMixin), and its handleRecipe goes to the buffer's own, where handlersFor is already hooked. PatternBufferIngredients (grouped search) reads the marks of the proxy's buffer too.