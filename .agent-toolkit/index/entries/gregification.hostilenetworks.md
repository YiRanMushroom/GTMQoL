+++
title = "Hostile Neural Networks source: simulation chamber and loot fabricator from HNN's data models"
kind = "design"
keys = [ "data model", "hnn", "hostile neural networks", "loot fabricator", "prediction", "recipe type trait", "simulation chamber" ]
updated = "2026-11-09 07:13:05"
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
User's design (2026-11-08): single blocks only (so modular multiblocks too); simulation chamber crafted with field generators, loot fabricator with a sensor and an emitter (shaped, hull counterpart, HNN's machine as an ingredient). HNN has no recipes: custom recipe logic over DataModelRegistry (a Placebo datapack registry). Sim chamber: 300 ticks at simCost FE/t (GT's feToEuRatio), model notConsumable by its DATA_MODEL component (data may change), matrix = model.input(), base drop, predictions = accuracy (whole + fraction as chance). Circuit 1 = training only (no matrix, no outputs, same energy), from any tier including Faulty (user's call; simulation needs a tier HNN can simulate), only while canLearn (HNN's simModelUpgrade rules). The recipe viewer shows a line on both (SimulationChamberLogic.INFO, a RecipeUIModifier reading the recipe data); representative training recipes start at the min tier. SimulationTraining (trait on every machine running the type, MachineFeatures.onRecipeType) fits a run after GT's parallels: simulates each craft on a model copy, cuts training runs to crafts that still teach, sets predictions to the sum of per-craft accuracies (the fraction as a chance per craft: GT's ChanceLogic rolls chance × totalRuns, so a chance holding the whole fraction came out crafts times too high), teaches the model in afterWorking only when the run finished (progress >= duration; interrupts also call afterWorking) and the model still has the data the run was made for; the next run, made for older model data, is looked up again in beforeWorking (hnn_model_data in the run's data). Pattern wear stays at the start (beforeWorking), when GT takes the inputs. Loot fabricator: 60 ticks at fabPowerCost, a ghost mark picks the drop (none for single-drop models). Compiled against the 6.3.0 jar in libs/ (ATM10's version, not on Modrinth for 1.21.1). 1.20 port: HNN 5.3.3 (CachedModel, enum tiers, accuracy at most 1, NBT); see the same entry there.