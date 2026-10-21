+++
title = "Ghost marks are not bypass-distinct; pattern buffers need Shared Hatches to use a ghost hatch's marks"
keys = [ "dyed bus", "insufficient", "mark pattern outputs", "patternoutputmarks", "sharedhatches", "shouldbypassdistinct" ]
updated = "2026-10-21 08:10:57"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeCapability.java"
snippet = "public boolean shouldBypassDistinct() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = "lang.addRawLang(key + \"tooltip.pattern_buffer\", \"With an ME Pattern Buffer, turn on its Shared Hatches.\");"
+++
An undyed ghost hatch is a normal non-distinct part (shouldBypassDistinct = false): gtceu adds it to the undyed and every dyed group, not to distinct buses. gtceu's pattern buffer only runs a recipe one slot fully provides, so a ghost input was "insufficient". Shared Hatches (a mixin on every MEPatternBufferPartMachine, on by default; proxies follow) lets a slot take the rest from shared parts. Alternative: the overclocked buffer's Mark Pattern Outputs (off by default).