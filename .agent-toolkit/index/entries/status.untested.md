+++
title = "Untested changes the user still has to check in game; each item is linked to its Untested: comment"
kind = "status"
keys = [ "in-game check", "remind", "untested" ]
updated = "2026-11-07 10:49:23"
checked = "2026-11-07"

[[items]]
name = "legacy-parts-no-unification"
value = "Legacy part recipes no longer skip NO_UNIFICATION materials (as on 1.21): look for extra extruder recipes on such materials (recipes.legacy-parts)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "// Untested: NO_UNIFICATION materials are no longer skipped (as on 1.21); check for extra extruder recipes"

[[items]]
name = "rotor-amount"
value = "Rotor material amount (M*4) now set from the TagPrefix RegisterEvent: a rotor's material info should show 4 units."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/part/LegacyPartRecipes.java"
snippet = "// Untested: moved from IGTAddon.registerTagPrefixes; a rotor's material info should show 4 units"

[[items]]
name = "kubejs-recipe-keys"
value = "KubeJS recipe keys now come from a KJSRecipeKeyEvent listener: with KubeJS, a crystal_growth script recipe with a block input should load (multiblock.kubejs-capabilities)."
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "// Untested: with KubeJS, a crystal_growth script recipe with a block input should load"

[[items]]
name = "ldlib-repo"
value = "LDLib's maven repo removed from build.gradle (build passes): if runClient can't resolve a dependency, put it back."
path = "build.gradle"
snippet = "// Untested: LDLib's maven repo was removed (v8 has no LDLib); if runClient can't resolve a dependency, put it back"

[[items]]
name = "client-run"
value = "First client run under ModDevGradle legacyForge (migration 3b63374; compileJava and runData pass): run config and args from build.gradle runs."
path = "build.gradle"
snippet = "// Untested: the client run under ModDevGradle (only the data run has been used so far)"

[[items]]
name = "evalex-runtime"
value = "EvalEx (jar-in-jar of ModularUI, used by GTCEu's PipeBlock) is plain runtimeOnly: no NoClassDefFoundError com/ezylang/evalex, gtceu:*_wire entries register."
path = "build.gradle"
snippet = "// Untested: in a client run, no NoClassDefFoundError com/ezylang/evalex and gtceu:*_wire entries register"

[[items]]
name = "productivelib"
value = "ProductiveLib (jar-in-jar in Productive Bees, published nowhere) isn't declared: does the dev client load it from PB's jar, or does PB fail to start?"
path = "build.gradle"
snippet = "// Untested: whether the dev client loads ProductiveLib from PB's jar or PB fails to start"

[[items]]
name = "jade-this0"
value = "Loom needed nameSyntheticMembers = true or Jade's StringRenderOutputMixin failed (@Shadow this$0 in Font$StringRenderOutput); MDG/parchment has no such switch: check Jade loads."
path = "build.gradle"
snippet = "// Untested: Jade loads (Loom needed nameSyntheticMembers for its this$0 @Shadow; MDG has no such switch)"

[[items]]
name = "autobuild-abilities"
value = "Abilities now recognized by machine block, not predicate name (GTCEu #5337 renamed them): auto-build leaves hatch/bus/muffler spots empty and places casings elsewhere."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "// Untested: abilities by machine block"

[[items]]
name = "autobuild-5337"
value = "Planner reworked for GTCEu #5337 (predicate filtering instead of count overrides): auto-build a fixed and an expandable multiblock, and one with a block choice (autobuild.planner)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "// Untested: filtering for GTCEu #5337"

[[items]]
name = "autobuild-preview"
value = "Auto-build settings are now GTCEu's multiblock preview (autobuild.panel): opens inside the machine UI, turned like the controller; layers, sizes, block per char and per clicked position are used by the build; Reset clears them. The old preview was buggy, see if #5337 fixed it."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildPanel.java"
snippet = "// Untested: GTCEu's preview inside our machine UI panel"

[[items]]
name = "steam-ctor-327"
value = "GTCEu 327: SimpleSteamMachineMixin injects only the full constructor; a steam single block shows one ghost circuit slot and the extra tanks, both kept after reload"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/SimpleSteamMachineMixin.java"
snippet = "// Untested: a steam single block (e.g. steam macerator) shows one ghost circuit slot and the extra fluid tanks,"

[[items]]
name = "mui-25"
value = "ModularUI build 25 (with GTCEu 327): no NoSuchMethodError from brachy.modularui; fluid slots update while a tank fills"
path = "gradle.properties"
snippet = "# Untested: build 25 (only FluidSlotSyncHandler changed"

[[items]]
name = "dummy-chunk-clear"
value = "DummyChunkSourceMixin without the clear() wrapper (1.20.1 MUI has none): client starts without its mixin error, multiblock preview renders"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/DummyChunkSourceMixin.java"
snippet = " * Untested: the client starts without a mixin error for DummyChunkSourceMixin"

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
value = "Everything of the pattern crafter on 1.20 (only compiled and loaded in data generation): encoding window, EMI/JEI fill, crafting, AE2 patterns"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternCrafter.java"
snippet = "// Untested: everything on 1.20: encoding window"

[[items]]
name = "wireless-fe"
value = "Energy cube side squares, overlays and FE in/out, wireless energy cell on an ME network (several cells count once, overlay gone on connected sides), new recipes"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/wireless/energy/WirelessFEMachines.java"
snippet = "// Untested: the cube's side squares and overlays"

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