+++
title = "Ghost marks are not bypass-distinct; pattern buffers need Shared Hatches to use a ghost hatch's marks"
keys = [ "dyed bus", "insufficient", "mark pattern outputs", "patternoutputmarks", "sharedhatches", "shouldbypassdistinct" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredientRecipeCapability.java"
snippet = "public boolean shouldBypassDistinct() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ghost/GhostIngredients.java"
snippet = "lang.addRawLang(key + \"tooltip.pattern_buffer\", \"With an ME Pattern Buffer, turn on its Shared Hatches.\");"
+++
A ghost hatch is in the shared (undyed, non-distinct) group like a normal bus; a distinct or dyed bus group only sees marks in its own group. gtceu's pattern buffer only runs a recipe one slot fully provides, so a recipe with a ghost input was "insufficient"; our buffers' Shared Hatches (integration/ae2/SharedHatches, on by default) let a slot take the rest from shared parts. Alternative: the overclocked buffer's Mark Pattern Outputs (PatternOutputMarks, off by default).