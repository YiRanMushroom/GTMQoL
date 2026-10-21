+++
title = "ME universal input/output parts: stocking input and dual output that also take bridged keys (chemicals)"
keys = [ "any_parts", "configuredelsewhere", "ingredientconverters", "me_universal_input", "me_universal_output", "stack-like ae", "universalconfigwidget" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "ME_UNIVERSAL_INPUT = GTMQoLAddon.machine(\"me_universal_input\", MEUniversalInputPartMachine::new)"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEUniversalInputPartMachine.java"
snippet = "public class MEUniversalInputPartMachine extends MEInputBusPartMachine implements IMEStockingPart {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/MEUniversalOutputPartMachine.java"
snippet = "public class MEUniversalOutputPartMachine extends MEDualOutputPartMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/StockingParts.java"
snippet = "public static boolean configuredElsewhere(MultiblockPartMachine self, GenericStack config) {"
+++
Both carry item/fluid import/export abilities only (no chemical ability), hence gregified Mek multiblocks use MultiblockShape.ANY_PARTS. Output puts bridged keys into MEDualOutputPartMachine's non-item buffer. UniversalConfigWidget takes JEI drags (AE2 IngredientConverters) and EMI ones (EmiStackConverters). MEStockingBus/HatchPartMachineMixin make gtceu's stocking parts ask configuredElsewhere too. The ME dual input (MEDualInputPartMachine) is the item/fluid-only sibling.