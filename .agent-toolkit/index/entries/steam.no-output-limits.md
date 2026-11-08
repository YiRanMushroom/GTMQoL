+++
title = "No output limits: steam macerator, steam grinder and steam oven keep all outputs (byproducts)"
keys = [ "addoutputlimit", "nooutputlimits", "require", "steam tweaks", "steamtweaks.nooutputlimits" ]
updated = "2026-11-08 19:27:14"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/SteamTweaks.java"
snippet = "private static void removeOutputLimits() {"
+++
The user wants byproducts everywhere. SteamTweaks.removeOutputLimits drops the item limit from the four definitions at setup: the limit maps stay live and are only read when a machine is built or runs (getOutputSize, IVoidable). Generator output limits (in GTMachineUtils) are untouched. If GTCEu adds a limit to another steam machine, add its definition there. GTMultiMachinesMixin only holds the fusion energy hatch change now.