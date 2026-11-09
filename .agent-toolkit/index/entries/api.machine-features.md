+++
title = "MachineFeatures: what we add to GTCEu's and other addons' machines, declared in one place, not a mixin per class"
kind = "design"
keys = [ "attach trait", "machine features", "panel configurator", "persistent trait" ]
updated = "2026-11-09 05:00:58"
checked = "2026-11-09"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/api/machine/MachineFeatures.java"
snippet = "public final class MachineFeatures {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "instanceFactory = MachineFeatures.wrap(instanceFactory);"
+++
User's idea (2026-11-08): one hook into machine creation, features declared as data. MachineBuilderMixin wraps every builder's instance factory at register() HEAD (the BE factory lambda reads the field when it runs), so onCreated runs after the whole constructor, on both sides, before saved data is read: the place for persistent traits. onPanel runs from MachineUIPanelBuilderMixin, also for panels without trait configurators. Used by SteamTweaks (circuit slot, tanks, bus circuit) and AutoBuildButton. onRecipeType: for every machine whose definition runs a recipe type (single block, modular multiblock, other addons'): behaviour a recipe type needs from its machine lives in a trait attached there (GT's IRecipeLogicModifierTrait: modifyRecipe after the definition's modifiers and parallels, beforeWorking when a run starts), so modular versions get it too; definition modifiers and SingleblockFeatures don't reach modular machines. PatternWear (pattern crafter tools) and SimulationTraining (HNN) work this way.