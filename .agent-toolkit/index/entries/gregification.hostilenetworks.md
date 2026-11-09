+++
title = "Hostile Neural Networks source: simulation chamber and loot fabricator from HNN 5's data models"
kind = "design"
keys = [ "data model", "hnn", "hostile neural networks", "loot fabricator", "prediction", "recipe type trait", "simulation chamber" ]
updated = "2026-11-09 05:24:22"
checked = "2026-11-09"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/HostileNetworksGregification.java"
snippet = "public final class HostileNetworksGregification {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/SimulationChamberLogic.java"
snippet = "enum SimulationChamberLogic implements GTRecipeType.ICustomRecipeLogic {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/SimulationTraining.java"
snippet = "final class SimulationTraining extends MachineTrait implements IRecipeLogicModifierTrait {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/gregification/hostilenetworks/LootFabricatorLogic.java"
snippet = "enum LootFabricatorLogic implements GTRecipeType.ICustomRecipeLogic {"
+++
Port of the 1.21 design (same entry there): custom recipe logic over HNN's DataModelRegistry, circuit 1 trains from Faulty on, SimulationTraining (MachineFeatures.onRecipeType) fits runs to parallels and teaches the model in beforeWorking; recipe viewer lines via SimulationChamberLogic.INFO. HNN 5.3.3 differences: CachedModel instead of DataModelInstance, ModelTier enum (FAULTY can't simulate, max = tier.next() == tier, representative simulations at BASIC), accuracy at most 1, model and prediction identified by NBT data_model.id (PartialNBTIngredient), the matrix is DataModel.input() as an ItemStack (DataModelItem.matchesInput). 1.20.1's trait interface has no getFailReason: RecipeLogic.putFailureReason. Modrinth: HNN 6ByGTKSc, Placebo 6SkuAGoz.