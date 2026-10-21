+++
title = "Bridged stack-like keys (chemicals) in gtceu's ME pattern buffer: per-slot handlers, aggregates, proxies"
keys = [ "coerce", "internalslot", "mepatternbufferproxystacklikemixin", "mepatternbufferworkerslotaccessor", "stack-like ae", "this$0" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferRecipeHandlerListMixin.java"
snippet = "@Inject(method = \"handlersFor\", at = @At(\"HEAD\"), cancellable = true)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferStackLikeSlotMixin.java"
snippet = "public abstract class MEPatternBufferStackLikeSlotMixin implements PatternBufferStackLikeSlot {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferStackLikeMixin.java"
snippet = "public abstract class MEPatternBufferStackLikeMixin implements PatternBufferStackLikeHolder {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/PatternBufferStackLike.java"
snippet = "public final class PatternBufferStackLike {"
+++
AE2MixinPlugin applies all MEPatternBuffer* mixins whenever AE2 is loaded; no effect without a bridge. gtceu is :slim here with no AT for it: package-private Worker is @Coerce Object + MEPatternBufferWorkerSlotAccessor, InternalSlot reaches its buffer via @Shadow this$0, refund builds IActionSource.ofMachine(getMainNode()::getNode) (no getActionSource on 1.20.1). Accepted unclean: unattached per-slot traits; aggregate parallel limit sums all slots, like gtceu for items.