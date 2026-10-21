+++
title = "Pattern encoding from the recipe viewer: prefer universal circuits, skip non-consumed (chance 0) inputs"
keys = [ "ae2.universalcircuitencoding", "not consumed", "skipnotconsumedinputs" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EncodingHelperMixin.java"
snippet = "public class EncodingHelperMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EmiEncodePatternHandlerMixin.java"
snippet = "        if (!GTMQoLConfig.get().ae2.skipNotConsumedInputs) return inputs;"
+++
Universal circuit: early switch ae2.universalCircuitEncoding (restart). Skip not consumed: runtime toma config, EMI only (applied only when EMI is loaded); relies on GT's GTEmiRecipe passing content chance through. AE2 19 has no JEI module, so 1.21 has no JEI variant (1.20.1 has a JEI mixin too).