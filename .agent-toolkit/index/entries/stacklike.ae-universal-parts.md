+++
title = "ME universal input/output parts: stocking input and dual output that also take bridged keys (chemicals)"
keys = [ "aekeydisplaywidget", "any_parts", "configuredelsewhere", "me_universal_input", "me_universal_output", "stack-like ae", "universalconfigwidget" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEUniversalInputPartMachine.java"
snippet = "public class MEUniversalInputPartMachine extends MEInputBusPartMachine implements IMEStockingPart {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEUniversalOutputPartMachine.java"
snippet = "public class MEUniversalOutputPartMachine extends MEDualOutputPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/StockingParts.java"
snippet = "public static boolean configuredElsewhere(MultiblockPartMachine self, GenericStack config) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "ME_UNIVERSAL_INPUT = GTMQoLAddon.machine(\"me_universal_input\", MEUniversalInputPartMachine::new)"
+++
The ME dual input is this input part without bridged keys; general part behaviour: ae2.dual-parts. Config widget drawn by AE's key renderers; recipe viewer drags go through AE's EMI converters. Both parts carry item/fluid import/export abilities only (no chemical ability), which is why gregified Mek multiblocks use MultiblockShape.ANY_PARTS. MEStockingBus/HatchPartMachineMixin make gtceu's own stocking parts ask configuredElsewhere too.