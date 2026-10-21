+++
title = "EmiCallWrapperMixin: machine UI recipe type button threw NoSuchMethodError (EmiApi.setPages invoker never applied)"
keys = [ "displayrecipecategory", "emiapiaccessor" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/EmiCallWrapperMixin.java"
snippet = "public class EmiCallWrapperMixin {"
+++
1.21 only. This mixin is why EMI is a `compileOnly` dependency (besides dev runtime).