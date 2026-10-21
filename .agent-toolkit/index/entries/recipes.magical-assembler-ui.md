+++
title = "Magical assembler UI: 4x4 items with a fluid column beside them via per-capability ModularUI layout builders"
keys = [ "gtrecipetypeuilayout", "inputcolumn", "setmachinecapabilitylayoutbuilder", "setrecipeviewerlayoutcapabilitylayoutbuilder", "steam tank" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/assembler/MagicalAssemblerUI.java"
snippet = "static GTRecipeTypeUILayout.Builder apply(GTRecipeTypeUILayout.Builder builder) {"
+++
`setLayoutGridBuilder` (String[] with 's' slots) can reshape grids, but every capability is stacked vertically in inputColumn, so fluids *beside* items need the custom per-capability builders. The steam-tank skip matters for the steam variant of the machine (AdvancedSteamMachines).