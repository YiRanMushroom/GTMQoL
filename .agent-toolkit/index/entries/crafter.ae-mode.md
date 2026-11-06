+++
title = "Pattern crafter AE mode (not built): how AE2 crafting patterns could reach the multiblock through GTCEu's pattern buffer"
kind = "design"
keys = [ "ae mode", "crafter", "crafting pattern", "imolecularassemblersupportedpattern", "molecular assembler", "pattern buffer" ]
updated = "2026-11-06 05:38:38"
checked = "2026-11-06"
+++
AE2 patterns placed in the machine by hand work through AECrafterPattern (processing patterns are refused: no recipe backs their outputs). Below is the part not built: pattern pushing from the ME network. Read from the code on 2026-11-05 (AE2 19.2.18, GTCEu 8.0.0 build 133).

- GTCEu's pattern buffer already takes AE2 crafting patterns: its slot filter is any EncodedPatternItem, and `pushPattern` never checks `supportsPushInputsToExternalInventory`. Only AE2's own PatternProviderLogic stops them; the crafting CPU calls `provider.pushPattern` directly.
- So the logic could read the worker's pattern through an ICrafterPattern adapter returned from `ICrafterPattern.decode` (IMolecularAssemblerSupportedPattern also covers AE2's stonecutting and smithing patterns; `getInputs`/`getOutputs` already are the recipe's lists) and build a recipe like PatternCrafterLogic does.
- A buffer has one worker, plus one per proxy: a second pattern waits until the first is used up. Only a speed limit. A dedicated hatch would give each pattern its own worker.
- The CPU also waits for remainders (`job.waitingFor` gets `expectedContainerItems`): they must return to the network exactly as `getRemainingKey` says, damaged tools included, or the job never ends. So AE mode can't use the not-consumed rule of our own patterns as is.
- A multiblock runs one recipe at a time: good for bulk, slower than an assembler matrix for many different patterns.
- Alternative: expose AE2's ICraftingMachine capability on a part, which a neighbouring pattern provider pushes to.