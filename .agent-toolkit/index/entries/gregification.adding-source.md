+++
title = "How to add a gregification type or a source for a new mod, and where to look in upstream sources"
keys = [ "gregification core" ]
updated = "2026-10-19 20:12:37"
+++
## A type for an existing source
Add an entry to its `types()` list (Mek, Create, PB): slot counts, sound/UI, counterpart and shape, plus a converter if the recipe class differs.

## A source for a new mod
1. `gregification/<mod>/<Mod>Gregification` with `static List<ForeignMachineType> types()`. Touch the mod's classes only there.
2. Converters per foreign recipe class (inputs, outputs, `EUt`, `duration`), taking `Recipe<?>`. Usually a `ForeignRecipeConverter.Single` wrapped with `ForeignRecipeConverter.single`; a full converter when one foreign recipe becomes several. Return false for anything a GT machine can't do.
3. A config toggle in `GTMQoLConfig.Gregification` (toma, GAME_RESTART), and in the `GTMQoL` constructor `if (config.gregification.<mod> && ModList.get().isLoaded("<mod>")) Gregification.addSource(<Mod>Gregification::types);`.
4. Dependency `modCompileOnly` + `modLocalRuntime` (Modrinth, `transitive = false`), not in `mods.toml`.
5. Recipes using something that isn't an item/fluid need a recipe capability first (stack-like).
6. A new structure goes in `MultiblockShape` (see gregification.multiblock-shape).

## Where to look upstream
- gtceu 1.20.1 sources in `.gtceu-src/com/gregtechceu/gtceu/`: `api/recipe/lookup/RecipeManagerHandler.java` (what gregification.proxy-mixin replaces), `api/recipe/GTRecipeType.java` (`getProxyRecipes`, `toGTrecipe`), `common/data/machines/GTMultiMachines.java` (patterns), `common/data/GTRecipeModifiers.java`, `api/registry/GTRegistry.java`.
- Mekanism 10.4 sources jar: `mekanism/api/recipes/`, `MekanismRecipeType`, `mekanism/common/tile/machine/TileEntity*` (10.4 passes most tick counts as literals to the super constructor).
- Create 6.0.8: `com.simibubi.create.content.processing.recipe.ProcessingRecipe`, `...sequenced.SequencedAssemblyRecipe`, `CreateBuiltInRegistries.FAN_PROCESSING_TYPE`.
- Productive Bees 1.20.1-12.6.0 jar: `common/block/entity/` machines, `util/BeeHelper`, `common/item/{BeeCage,Gene,HoneyTreat}`.