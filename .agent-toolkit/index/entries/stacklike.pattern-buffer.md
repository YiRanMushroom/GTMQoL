+++
title = "Bridged stack-like keys (chemicals) in gtceu's ME pattern buffer: per-slot handlers, aggregates, proxies"
keys = [ "gtmqol:stack_like", "internalslot", "mepatternbufferproxystacklikemixin", "mepatternbufferstacklikemixin", "stack-like ae", "this$0" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/PatternBufferStackLike.java"
snippet = "public final class PatternBufferStackLike {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferStackLikeSlotMixin.java"
snippet = "public abstract class MEPatternBufferStackLikeSlotMixin implements PatternBufferStackLikeSlot {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferRecipeHandlerListMixin.java"
snippet = "@Inject(method = \"handlersFor\", at = @At(\"HEAD\"), cancellable = true)"

[[links]]
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker slot"
+++
Applies to both buffers whenever AE2 is loaded. Unclean points the user accepted: unattached per-slot traits (like gtceu's); @Shadow this$0 on InternalSlot (javac hides synthetic members, an AT can't expose it); the aggregate's parallel limit sums all slots, as gtceu does for items. workers/Worker/Worker.slot are public through our AT (gtceu is accessCompileOnly + runtimeOnly). Outputs: PatternBufferReturn; recipedb split: PatternBufferIngredients.