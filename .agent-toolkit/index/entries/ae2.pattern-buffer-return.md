+++
title = "Pattern buffer return: multiblock outputs go straight into the ME network (extra HIGHEST-priority OUT handler list)"
keys = [ "ae2.patternbufferreturn", "crafting cpu", "getrecipehandlers", "iactionsource" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MEPatternBufferReturnMixin.java"
snippet = "    private List<RecipeHandlerList> gtmqol$addReturnHandlers(List<RecipeHandlerList> original) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/PatternBufferReturn.java"
snippet = "public final class PatternBufferReturn {"

[[items]]
name = "1.20.1-mutable-ingredients"
value = "1.20.1: GTCEu's SizedIngredient / FluidIngredient / ItemStack are mutable, so the unfinished rest is written back with setAmount/setCount in place instead of replacing the list entry (1.21). Same in MEDualOutputPartMachine."
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/PatternBufferReturn.java"
snippet = "sized.setAmount((int) remaining);"
+++
Per-buffer toggle ("ME" left configurator, default on) plus the global switch ae2.patternBufferReturn, both checked on every call because the list must keep its identity (the multiblock removes it by identity on unform). The buffer's actionSource is protected on 1.20.1, so each handler builds IActionSource.ofMachine from the main node. Its lang lives in AEDualParts.initLang, registered only when ae2.patternBufferReturn is on at startup.