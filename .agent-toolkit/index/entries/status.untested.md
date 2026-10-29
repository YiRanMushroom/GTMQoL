+++
title = "Untested changes the user still has to check in game; each item is linked to its Untested: comment"
kind = "status"
keys = [ "in-game check", "remind", "untested" ]
updated = "2026-10-27 15:15:44"
checked = "2026-10-29"

[[items]]
name = "rotor-amount"
value = "GTCEu build 130 (#5414): rotor material amount (M*4) now set on PostMaterialEvent: a rotor's material info should show 4 units (recipes.legacy-parts)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "// Untested: now set on PostMaterialEvent"

[[items]]
name = "steam-machine-keys"
value = "Steam single-block recipe keys are suppliers (materials registered after construction): the LP/HP steam wiremill, lathe, mixer, extruder, centrifuge crafting recipes should still exist."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "// Untested: keys are suppliers now"

[[items]]
name = "pattern-buffer-count"
value = "Our count fix replaced by upstream #5568: a pattern needing 2+ items or more than 1 mB runs from the pattern buffer, also with parallel (gtceufix.pattern-buffer-internal-slot)."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/MEPatternBufferInternalSlotMixin.java"
snippet = "// Untested: upstream #5568 replaced our count fix"

[[items]]
name = "autobuild-abilities"
value = "Abilities now recognized by machine block, not predicate name: auto-build leaves hatch/bus/muffler spots empty and places casings elsewhere (autobuild.planner)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/BuildOptions.java"
snippet = "// Untested: abilities by machine block"
+++
Remind the user of these at the start of a session. Once one is checked: delete its Untested: comment; the item goes stale, then remove it (index_update remove_items).