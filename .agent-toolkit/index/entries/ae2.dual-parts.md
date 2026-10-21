+++
title = "ME dual / universal input and output parts (LuV, 36 slots): legacy dual-input migration, dual output buffers"
keys = [ "ae2.dualhatches", "data stick", "fluid_config", "fluidconfigstacks", "me_dual_input", "me_dual_output", "me_universal_input", "me_universal_output", "stocking" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "public final class AEDualParts {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEDualInputPartMachine.java"
snippet = "public class MEDualInputPartMachine extends MEUniversalInputPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEDualOutputPartMachine.java"
snippet = "public class MEDualOutputPartMachine extends MEOutputBusPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEUniversalInputPartMachine.java"
snippet = "        void keepSlots(ExportOnlyAEItemSlot[] full) {"

[[items]]
name = "aeconfigsync-not-ported"
value = "1.21's AEConfigSyncHandlerMixin is not ported: ModularUI 1.20.1's syncToClient writes eagerly, so the bug probably doesn't exist here. Port it if a stocking bus / dual part config UI doesn't update while open."
+++
Config ae2.dualHatches. keepSlots (from load): GTCEu loads a saved slot array at its saved length, so the 36-slot array is put back. Old dual inputs (stocking bus + 16-slot fluid trait) migrate in MEDualInputPartMachine.load. Copy/paste uses the stocking bus format over 36 slots, so stocking bus, dual and universal paste into each other. Recipes: ME assembler, or assembler if ae2.processing off.