+++
title = "Untested changes the user still has to check in game; each item is linked to its Untested: comment"
kind = "status"
keys = [ "in-game check", "remind", "untested" ]
updated = "2026-11-07 09:29:01"
checked = "2026-11-07"

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

[[items]]
name = "research-oc"
value = "Research OC (oc.research): a research station one tier above the recipe shows (EMI) and takes half the total CWU at 4x EU/t."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ResearchOverclockMixin.java"
snippet = "// Untested: a research station one tier above the recipe shows and takes half the total CWU at 4x EU/t"

[[items]]
name = "wireless-channels"
value = "Wireless channels (wireless.channels): channel list (any length, scrolls), create/edit/delete, private/team/public; HPCA -> research station over a channel; 2nd receiver refused without router, allowed with; data bank -> assembly line; bindings survive reload."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/channel/ChannelMachines.java"
snippet = "// Untested: channel UI (list, create/edit/delete, private/team/public); an HPCA's wireless"

[[items]]
name = "coil-oc"
value = "Coil OC as equivalent OC (oc.overhaul): an EBF with inputs for fewer than 8 parallels still goes to 1 tick, using more energy."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/OverclockingLogicMixin.java"
snippet = "// Untested: an EBF whose inputs allow fewer than 8 parallels still reaches 1 tick (energy use rises accordingly)"

[[items]]
name = "pending-output-bulk"
value = "Fishing pond treasure (multiblock.pending-output): thousands of pending unstackables on an ME output bus no longer lag."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/PendingOutputTrait.java"
snippet = "// Untested: a treasure fishing pond with thousands of pending unstackables on an ME output bus no longer lags"

[[items]]
name = "void-miner-limits"
value = "Void miner (multiblock.void-miner): settings go up to the config limits (64/4096), Ctrl doubles/halves."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/VoidMinerMachine.java"
snippet = "// Untested: the settings popup goes up to the config limits (64 / 4096 by default); Ctrl doubles/halves"

[[items]]
name = "pattern-crafter"
value = "JEI's fill button; exact and substitutes; tool wear per parallel, runs cut to what the tool has left; the duplicates button; 9 pattern slots, product and return slots"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafter.java"
snippet = "// Untested: JEI's fill button puts crafting"

[[items]]
name = "wireless-fe"
value = "Energy cube side squares, overlays and FE in/out, wireless energy cell on an ME network (several cells count once, overlay gone on connected sides), new recipes"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessFEMachines.java"
snippet = "// Untested: the cube's side squares and overlays"

[[items]]
name = "bloom-rebuild"
value = "No more Not building! or sortQuads crashes; glowing blocks still glow and stop when broken"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/BloomSafeModeMixin.java"
snippet = "// Untested: no more \"Not building!\""

[[items]]
name = "smart-doubling-batches"
value = "Whole batches from stock, pieces for what waits or lacks power, restart, x2 settings, chains"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/BatchPlan.java"
snippet = "// Untested: an order from stock goes out as one"

[[items]]
name = "crafting-planner"
value = "Plans in game against the preview, missing lists, huge orders, loops, container items"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/AECraftingPlanner.java"
snippet = "// Untested: plans in game match the preview"

[[items]]
name = "pattern-matrix"
value = "The whole machine in game"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrix.java"
snippet = "// Untested: all of it in game"

[[items]]
name = "matrix-interface-ui"
value = "Matrix ME Interface window: rename, auto upload shared with the controller"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/MatrixInterfaceMachine.java"
snippet = "// Untested: renaming shows in the pattern access terminal"

[[items]]
name = "matrix-cancel"
value = "Pattern matrix cancel button gives back the inputs of crafts not done"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrixWork.java"
snippet = "// Untested: Shift-click on the controller's cancel button"

[[items]]
name = "partial-push"
value = "AE2's CPU pushes part of a batch: a chain's next step starts as the step before delivers; a batch needing more power than the network stores goes in parts; the rest shows in the CPU list and survives a restart"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
snippet = "// Untested: a chain's next step starts with part of a batch as the step before delivers"

[[items]]
name = "matrix-controller-broken"
value = "Break the matrix controller mid-job: undone crafts' inputs and finished items go to the network, what it can't take is gone"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/PatternMatrixWork.java"
snippet = "// Untested: breaking the controller mid-job sends undone crafts' inputs and finished items to the network"
+++
Remind the user of these at the start of a session. Once one is checked: delete its Untested: comment; the item goes stale, then remove it (index_update remove_items).