+++
title = "Pattern buffer return: multiblock outputs go straight into the ME network (extra HIGHEST-priority OUT handler list)"
keys = [ "ae2.patternbufferreturn", "crafting cpu", "getrecipehandlers" ]
updated = "2026-10-19 20:08:33"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/PatternBufferReturn.java"
snippet = "public final class PatternBufferReturn {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MEPatternBufferReturnMixin.java"
snippet = "    private List<RecipeHandlerList> gtmqol$addReturnHandlers(List<RecipeHandlerList> original) {"
+++
Per-buffer toggle ("ME" left configurator, default on) plus the global runtime switch ae2.patternBufferReturn, both checked on every call because the list must keep its identity (the multiblock removes it by identity on unform). Its lang lives in AEDualParts.initLang.