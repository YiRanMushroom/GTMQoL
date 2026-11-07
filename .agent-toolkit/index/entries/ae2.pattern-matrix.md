+++
title = "Pattern Matrix: multiblock crafting AE2 and our patterns in bulk, tier from a machine casing frame"
kind = "design"
keys = [ "assembler matrix", "catalyst", "pattern matrix", "tiered multiblock" ]
updated = "2026-11-07 09:15:56"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrix.java"
snippet = "public final class PatternMatrix"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrixWork.java"
snippet = "final class PatternMatrixWork"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/CrafterPatternDetails.java"
snippet = "public final class CrafterPatternDetails"
+++
User's design; batches from ae2.fast-crafting. Capacity: processing cores x 4 x 4^tier crafts per tick, patterns taking turns. Catalysts only for our patterns. Terminal patterns upload by themselves (no EAP); the switch lives in the controller, the interface's window (rename for terminals) toggles it too. Inputs kept per slot, used up as taken: Shift cancel returns exactly what's left. Recipes: ME assembler, controller shaped 3x3. Datagen: gtceufix MachineModelVariantOrderMixin.