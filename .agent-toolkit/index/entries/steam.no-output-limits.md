+++
title = "No output limits: steam macerator, steam grinder and steam oven keep all outputs (byproducts)"
keys = [ "addoutputlimit", "nooutputlimits", "require", "steam tweaks", "steamtweaks.nooutputlimits" ]
updated = "2026-10-21 08:13:50"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMachinesMixin.java"
snippet = "private static MachineBuilder gtmqol$noOutputLimit(MachineBuilder builder, RecipeCapability<?> cap, int limit,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMultiMachinesMixin.java"
snippet = "private static MachineBuilder gtmqol$noOutputLimit(MultiblockMachineBuilder builder, RecipeCapability<?> cap,"
+++
The user wants byproducts everywhere. Generator output limits (in GTMachineUtils) are untouched. require counts (GTMachinesMixin 1, GTMultiMachinesMixin 2) break at load if GTCEu changes its calls: recheck on bumps. The LV-HV macerator limits were removed upstream (#5528), so the steam macerator's is the only addOutputLimit left in GTMachines.