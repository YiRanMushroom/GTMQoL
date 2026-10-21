+++
title = "Industrial Large Chemical Reactor: LCR recipes x1/64 duration, parallel hatch, perfect subtick OC, batch; port of 2.0.0"
keys = [ "ilcr", "industrial large chemical reactor", "large_chemical_recipes" ]
updated = "2026-10-19 20:57:50"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static void initIndustrialLargeChemicalReactor() {"
+++
Ported from the 2.0.0 (GTCEu 7.x) Kotlin version: same structure (FactoryBlockPattern.start() became start(FRONT, UP, RIGHT), aisle -> slice, .or -> .and), same modifiers and the same magical assembler recipe (64 LCR + 16 B glue). The coils are part of the structure only; LCR recipes have no temperature. Config machines.industrialLargeChemicalReactor.