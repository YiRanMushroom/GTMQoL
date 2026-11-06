+++
title = "Pattern crafter bugs met: EMI handler recursion, tank count saved with old machines"
keys = [ "emiplayerinventory", "index out of bounds", "notifiablefluidtank", "pitfall", "stackoverflow", "tanks" ]
updated = "2026-11-06 18:49:21"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafterEmi.java"
snippet = "not EmiPlayerInventory.of"
+++
EmiPlayerInventory.of asks the handler, so calling it from getInventory overflowed the stack and crashed every MUI window: build the list from the player's items. A machine saves its whole tank array, so changing a machine's tank count gives `Index 2 out of bounds` on placed ones: replace them. Item storage resizes by itself.