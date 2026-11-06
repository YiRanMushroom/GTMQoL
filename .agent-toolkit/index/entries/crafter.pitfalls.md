+++
title = "Pattern crafter bugs met: GTItems in the constructor, EMI handler recursion, saved tank count"
keys = [ "emiplayerinventory", "gtitems", "notifiablefluidtank", "pitfall", "rundata hang", "stackoverflow", "tanks" ]
updated = "2026-11-06 18:49:31"
checked = "2026-11-06"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "GTItems.attach loads GTItems"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafterEmi.java"
snippet = "not EmiPlayerInventory.of"
+++
GTItems.attach loads GTItems, which needs the materials: from the mod constructor runData hung, so items register in onRegisterCovers. EmiPlayerInventory.of asks the handler, so calling it from getInventory overflowed the stack: build the list from the player's items. A machine saves its whole tank array, so changing a machine's tank count gives `Index 2 out of bounds` on placed ones: replace them.