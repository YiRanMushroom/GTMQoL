+++
title = "Untested changes the user still has to check in game; each item is linked to its Untested: comment"
kind = "status"
keys = [ "in-game check", "remind", "untested" ]
updated = "2026-11-08 22:04:48"
checked = "2026-11-08"

[[items]]
name = "rotor-amount"
value = "GTCEu build 130 (#5414): rotor material amount (M*4) now set on PostMaterialEvent: a rotor's material info should show 4 units (recipes.legacy-parts)."
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/LegacyPartRecipes.java"
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
value = "Coil OC: coils 900K over the recipe give one 8x OC and the rest 4x (1800K: two), at 4x EU/t each; an EBF with inputs for few parallels still goes to 1 tick"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/OverclockingLogicMixin.java"
snippet = "// Untested: coils 900K over the recipe give one 8x OC and the rest 4x (1800K: two); still 1 tick with few parallels"

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
name = "matrix-cancel"
value = "Assembling Matrix cancel button gives back the inputs of crafts not done"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
snippet = "// Untested: Shift-click on the controller's cancel button"

[[items]]
name = "partial-push"
value = "AE2's CPU pushes part of a batch: a chain's next step starts as the step before delivers; a batch needing more power than the network stores goes in parts; the rest shows in the CPU list and survives a restart"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ae2/CraftingCpuLogicBatchMixin.java"
snippet = "// Untested: a chain's next step starts with part of a batch as the step before delivers"

[[items]]
name = "matrix-controller-broken"
value = "Break the Assembling Matrix controller mid-job: undone crafts' inputs and finished items go to the network, what it can't take is gone"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
snippet = "// Untested: breaking the controller mid-job sends undone crafts' inputs and finished items to the network"

[[items]]
name = "machine-features"
value = "Steam single blocks keep circuit slot and tanks (also after reload), steam input bus circuit slot, auto-build button on controllers, now through MachineFeatures"
path = "src/main/java/com/yiran/minecraft/gtmqol/api/machine/MachineFeatures.java"
snippet = "// Untested: steam single blocks still get the circuit slot and extra tanks and keep them after a reload, the steam"

[[items]]
name = "potion-logic-runners"
value = "Brewery still brews splash/lingering potion fluids; EMI shows those and the canner potion recipes"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionMixBrewingLogic.java"
snippet = "// Untested: the brewery still brews splash and lingering potion fluids, and EMI still shows those recipes and the"

[[items]]
name = "recipe-type-layouts"
value = "Recipe viewer still shows chemical, block and ghost item slots and the potion any-mod hint"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/RecipeTypeLayouts.java"
snippet = "// Untested: recipe viewer pages still show chemical, block and ghost item slots, and potion inputs still flash the"

[[items]]
name = "decoded-patterns"
value = "Pattern decode cache: crafters, matrix, tooltips as before; /reload with changed recipe updates patterns"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/DecodedPatterns.java"
snippet = "// Untested: Pattern Crafters, the Assembling Matrix and pattern tooltips work as before, and after a /reload that"

[[items]]
name = "shift-drag"
value = "Shift + drag moves stacks in and out of GTCEu machine UIs; held-stack spreading unchanged"
path = "src/main/java/com/yiran/minecraft/gtmqol/client/ShiftDragQuickMove.java"
snippet = "// Untested: Shift + drag over the inventory moves stacks into a GTCEu machine, and over machine slots back out;"

[[items]]
name = "patterns-window-speed"
value = "Patterns window fast with thousands of patterns, icons and tooltips unchanged"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixPatternGridWidget.java"
snippet = "// Untested: the patterns window opens and updates quickly with thousands of patterns, icons and tooltips as before"

[[items]]
name = "matrix-fe-flush"
value = "Matrix speed/FE as before; short FE on shared-network hatches slows it instead of unpaid crafts; refused products arrive later"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixWork.java"
snippet = "    // Untested: crafting speed and FE use as before; with too little FE on hatches sharing one wireless network the"

[[items]]
name = "modular-lazy-kind"
value = "1.21: the game loads with GTCEu build 139 (checked by the user 2026-11-08); still to see: modular generators make EU, keep progress while waiting, LCE look when the single block has no front overlay"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachine.java"
snippet = "// Untested: modular generators (modular steam turbine) make EU and keep their progress while waiting"

[[items]]
name = "energy-cell-output-only"
value = "Wireless Energy Cell only gives power: an energy acceptor's power on the same ME network fills AE2's energy cells, not the wireless network; the ME network still runs from the wireless network"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/ae2/WirelessEnergyCellMachine.java"
snippet = "// Untested: an energy acceptor's power on the same ME network goes into AE2's energy cells, not the wireless network"

[[items]]
name = "matrix-upload-toast"
value = "Encoding a pattern for an Assembling Matrix shows a toast over the terminal (product icon and name); a pattern the matrix already has shows the copy text; encoding several in a row updates one toast"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/matrix/AssemblingMatrixUploadToast.java"
snippet = "// Untested: encoding a pattern for an Assembling Matrix shows the toast over the terminal; a copy shows the other text"

[[items]]
name = "potion-emi-exact"
value = "EMI: recipes for a splash potion fluid don't list the plain or lingering one's (and the other way round); filling a splash recipe into a Pattern Encoding Terminal takes the splash fluid"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionEmi.java"
snippet = "// Untested: in EMI, recipes for a splash potion fluid don't list the plain or lingering one's, and filling a splash"
+++
Remind the user of these at the start of a session. Once one is checked: delete its Untested: comment; the item goes stale, then remove it (index_update remove_items).