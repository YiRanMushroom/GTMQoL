+++
title = "Shared hatches: pattern buffer slots take what they lack from the machine's undyed / bypass-distinct parts"
keys = [ "bufferrecipehandlerlist", "circuit", "consumeworker", "gtmqol$sharedhatches", "mold", "proxy" ]
updated = "2026-10-19 20:12:36"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferSharedHatchesListMixin.java"
snippet = "    @ModifyReturnValue(method = \"consumeWorker\", at = @At(\"RETURN\"))"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/MEPatternBufferSharedHatchesMixin.java"
snippet = "    private boolean gtmqol$sharedHatches = true;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/RecipeRunnerSharedHatchesMixin.java"
snippet = "public abstract class RecipeRunnerSharedHatchesMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/SharedHatches.java"
snippet = "public final class SharedHatches {"
+++
Why: GTCEu only runs a buffer recipe from a slot that provides all of it, so circuits/molds/ghost marks in other buses never helped. Also Item/FluidRecipeCapabilitySharedHatchesMixin (parallel + "insufficient" checks) and MEPatternBufferProxySharedHatchesMixin (proxies follow their buffer). Ghost ingredient hatches need this on. All *SharedHatchesMixin classes always apply (AE2MixinPlugin, see ae2.mixin-plugin); the feature is toggled per buffer.