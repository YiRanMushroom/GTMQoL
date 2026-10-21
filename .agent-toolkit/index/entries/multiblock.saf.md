+++
title = "Smart Assembly Factory: assembly line recipes with parallel hatch, perfect subtick OC, batch; port of 7.x"
keys = [ "easysafrecipe", "smart assembly factory", "smart_assembly_factory" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static void initSmartAssemblyFactory() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "if (GTMQoLConfig.get().machines.easySAFRecipe) {"
+++
Port from 7.x: pattern start(BACK, UP, RIGHT) became v8 start(RIGHT, UP, BACK), setRepeatable(4) became sliceRepeatable(4, 4, ...). Controller sits after 1 + 4 slices, see multiblock.start-offset. Data hatches: multiblock.saf-data-hatches. machines.easySAFRecipe (default on) adds 16 assembly lines + circuit 24 in the magical assembler, 10 s at its default EU/t, besides the normal ZPM recipe.