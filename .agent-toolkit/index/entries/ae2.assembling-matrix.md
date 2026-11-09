+++
title = "Assembling Matrix: multiblock crafting AE2 and GTMQoL crafting patterns in bulk, tier from a machine casing frame"
kind = "design"
keys = [ "assembler matrix", "assembling matrix", "catalyst", "pattern matrix", "tiered multiblock" ]
updated = "2026-11-09 07:00:55"
checked = "2026-11-09"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrix.java"
snippet = "public final class AssemblingMatrix"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
snippet = "final class AssemblingMatrixWork"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/CraftingPatternDetails.java"
snippet = "public final class CraftingPatternDetails"
+++
User's design; called Pattern Matrix until 2026-11-07, renamed with its ids (assembling_matrix, assembling_matrix_interface/_pattern_core/_processing_core; config ae2.assemblingMatrix*). Batches from ae2.fast-crafting. Capacity: processing cores x 4 x 4^tier crafts per tick, patterns taking turns. Catalysts only for GTMQoL Crafting Patterns. Terminal patterns upload by themselves (no EAP); the switch lives in the controller, the interface's window (rename for terminals) toggles it too. Upload only on Shift-click Encode (user's call 2026-11-09): the client sends AssemblingMatrixUploadMessage.Request(shift) from the encode HEAD (client side), the server keeps it per player and the TAIL takes it. Outcomes: TAKEN (slot emptied), ALREADY_THERE and NO_ROOM (pattern stays, before: a copy became a blank), NOT_OURS (silent). The player sees a toast (AssemblingMatrixUploadMessage, play-to-client payload on 1.21 / SimpleChannel gtmqol:assembling_matrix on 1.20, then AssemblingMatrixUploadToast with the product's icon): the terminal covers the action bar, and a toast shown again replaces the visible one. Patterns window: a new search scrolls to the top, and the scroll is clamped every update (scrolled down, a search showed nothing). Inputs kept per slot, used up as taken: Shift cancel returns exactly what's left. Recipes: ME assembler, controller shaped 3x3. Datagen: gtceufix MachineModelVariantOrderMixin. Its texts follow skill.naming-and-text. 1.20: registers in two steps (build.branch-differences).