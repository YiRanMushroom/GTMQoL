+++
title = "Untested changes the user still has to check in game; each item is linked to its Untested: comment"
kind = "status"
keys = [ "in-game check", "remind", "untested" ]
updated = "2026-11-09 05:29:58"
checked = "2026-11-08"

[[items]]
name = "legacy-parts-no-unification"
value = "Legacy part recipes no longer skip NO_UNIFICATION materials (as on 1.21): look for extra extruder recipes on such materials (recipes.legacy-parts)."
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/LegacyPartRecipes.java"
snippet = "// Untested: NO_UNIFICATION materials are no longer skipped (as on 1.21); check for extra extruder recipes"

[[items]]
name = "rotor-amount"
value = "Rotor material amount (M*4) now set from the TagPrefix RegisterEvent: a rotor's material info should show 4 units."
path = "src/main/java/com/yiran/minecraft/gtmqol/data/recipe/LegacyPartRecipes.java"
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
value = "Brewery still brews splash/lingering potion fluids; JEI/EMI show those and the canner potion recipes"
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
value = "EMI: recipes for a splash potion fluid don't list the plain or lingering one's; filling a splash recipe into a Pattern Encoding Terminal takes the splash fluid"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/potion/PotionEmi.java"
snippet = "// Untested: in EMI, recipes for a splash potion fluid don't list the plain or lingering one's, and filling a splash"

[[items]]
name = "potion-jei-subtype"
value = "JEI without EMI: recipes for a splash potion fluid don't list the plain or lingering one's"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/PotionFluidSubtypeInterpreterMixin.java"
snippet = "// Untested: with JEI (no EMI), recipes for a splash potion fluid don't list the plain or lingering one's"

[[items]]
name = "pattern-shift-icon"
value = "Holding Shift shows Crafting Patterns as what they craft (inventory, pattern crafter, patterns window); chests keep the pattern look; releasing Shift brings the pattern back"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/CraftingPatternModel.java"
snippet = "// Untested: holding Shift shows Crafting Patterns as what they craft (inventory, pattern crafter, patterns window)"

[[items]]
name = "pattern-wear-modular"
value = "The modular pattern crafter wears tools like the single block; a run stops at the last usable tool (worn-out reason shown)"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crafter/PatternWear.java"
snippet = "// Untested: the modular pattern crafter wears tools like the single block, and a run stops at the last usable tool"

[[items]]
name = "hnn-training"
value = "HNN simulation chamber (and modular): a run of N parallels adds N data, predictions follow the rising accuracy, circuit 1 trains from Faulty on without a matrix and stops at max tier; EMI/JEI recipes show the learning line"
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/SimulationTraining.java"
snippet = "// the model and its predictions follow the rising accuracy; circuit 1 trains from Faulty on, without a matrix, and"

[[items]]
name = "hnn-loot-fabricator"
value = "HNN loot fabricator: the ghost mark picks the drop, single-drop models need none; the recipe viewer shows both machines' recipes; crafting recipes exist"
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/LootFabricatorLogic.java"
snippet = "// Untested: a prediction becomes the marked drop (a model with one drop needs no mark), EMI lists every model's drops"
+++
Remind the user of these at the start of a session. Once one is checked: delete its Untested: comment; the item goes stale, then remove it (index_update remove_items).