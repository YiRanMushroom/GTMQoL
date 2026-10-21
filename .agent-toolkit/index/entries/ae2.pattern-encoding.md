+++
title = "Pattern encoding from the recipe viewer (EMI and JEI): prefer universal circuits, skip non-consumed (chance 0) inputs"
keys = [ "ae2.universalcircuitencoding", "not consumed", "skipnotconsumedinputs" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EmiEncodePatternHandlerMixin.java"
snippet = "        if (!GTMQoLConfig.get().ae2.skipNotConsumedInputs) return inputs;"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EncodePatternTransferHandlerMixin.java"
snippet = "public class EncodePatternTransferHandlerMixin {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/EncodingHelperMixin.java"
snippet = "public class EncodingHelperMixin {"
+++
Universal circuit: early switch ae2.universalCircuitEncoding (restart). Skip not consumed: runtime toma config; EMI mixin applies only with EMI, JEI mixin only with JEI (both client mixins). JEI slots carry no chances, so the JEI mixin rebuilds the slot layout (item contents, fluid contents, then stack-like) and gives up if the count differs. 1.21 has only the EMI variant (AE2 19 has no JEI module).