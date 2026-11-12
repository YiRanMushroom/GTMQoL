+++
title = "Advanced Chip Engraving Factory: lensless laser engraver copies, wafers at 4x time and output, product as a ghost mark"
kind = "code"
keys = [ "advanced_chip_engraving_factory", "large engraving laser", "laser engraver", "lensless", "machines.advancedchipengravingfactory", "wafer" ]
updated = "2026-11-12 07:53:24"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/engraving/AdvancedChipEngraving.java"
snippet = "private static void addLenslessCopy(GTRecipeBuilder builder, RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/engraving/AdvancedChipEngraving.java"
snippet = "return MultiblockPatternBuilder.start(RIGHT, UP, BACK)"
+++
The user's design (2026-11-11): a GCYM-style multiblock (same modifiers as GTCEu's Large Engraving Laser: environment requirement, parallel hatch, non-perfect subtick OC, batch) running its own recipe type, copied from every laser engraver recipe like the Electric Implosion Compressor does (multiblock.electric-implosion): item inputs with chance 0 (the lens) removed and the first output as a ghost ingredient; only when an input is a wafer (an item or tag named *_wafer, read from the values: the user found x4 on everything too strong, 2026-11-12) duration x4 and every item output x4 (copyChanced, chanced ones too), other copies keep the laser engraver's time and outputs (ghost.capability), since without the lens recipes differing only in it share an input. Conditions are cleared (the user, 2026-11-12): no cleanroom or anything else. Tooltip line 2 (automation through Mark Pattern Outputs) is added only when AE2 is loaded and ae2.overclockedPatternBuffer is on; its lang is always generated. Structure: 11 slices of 7x7 left to right, end caps with a glass window, ribs, and four laser stations (front Tempered Glass, Tungstensteel Pipe heads over a Grate line), all air inside; no repeatable slice, so no startOffset. Controller (the user, 2026-11-12): magical assembler at IV, 1 Large Engraving Laser, 16 ZPM circuits, 64 IV Emitters, plus 64 Laser-Safe Engraving Casings, 64 double tungstensteel plates and 32 ingots of soldering alloy. 1.20: hook via LASER_ENGRAVER_RECIPES.onRecipeBuild, KubeJS recipes get no copy.