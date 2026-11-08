---
name: gregification-source
description: "Gregification: add a gregified machine type or a new mod as a source; use when gregifying another mod's machines"
---

<!-- agent-toolkit: generated from index entry skill.gregification-source: change it with index_update("skill.gregification-source"), not here -->

## A type for an existing source
Add an entry to its table (MI `SINGLE_BLOCKS`/`MULTIBLOCKS`) or `types()` list (Mek, Create, PB): slot counts, sound/UI, counterpart and shape, plus a converter if the recipe class differs.

## A source for a new mod
1. `gregification/<mod>/<Mod>Gregification` with `static List<ForeignMachineType> types()`. Touch the mod's classes only there.
2. Converters per foreign recipe class (inputs, outputs, `EUt`, `duration`). Usually a `ForeignRecipeConverter.Single` wrapped with `ForeignRecipeConverter.single`; a full converter when one foreign recipe becomes several. Return false for anything a GT machine can't do (conditions, missing ingredients).
3. A config toggle under `gregification.*`, and in the `GTMQoL` constructor `if (config.gregification.<mod> && ModList.get().isLoaded("<mod>")) Gregification.addSource(<Mod>Gregification::types);`.
4. Dependency `compileOnly` + `localRuntime`, not in `neoforge.mods.toml`.
5. Recipes using something that isn't an item/fluid need a recipe capability first (stack-like).
6. A new structure goes in `MultiblockShape` (see gregification.multiblock-shape).

## Where to look upstream
- gtceu 1.21 sources `.gtceu-src-1.21/com/gregtechceu/gtceu/`: `core/mixins/RecipeManagerLateMixin.java`, `api/recipe/lookup/RecipeManagerHandler.java` (what gregification.proxy-mixin replaces), `api/recipe/GTRecipeType.java` (`getProxyRecipes`, `toGTRecipe`), `common/data/machines/GTMultiMachines.java` (patterns), `common/data/GTRecipeModifiers.java`, `api/machine/mui/MachineUIPanelBuilder.java` (batch button).
- MI: the Modrinth jar in the Gradle cache, `aztech/modern_industrialization/machines/{init/MIMachineRecipeTypes,recipe/MachineRecipe}`.
- Mekanism sources jar: `mekanism/api/recipes/`, `MekanismRecipeTypes`, `mekanism/common/tile/machine/TileEntity*` (`BASE_TICKS_REQUIRED`, per-tick usage).
- Productive Bees: source clone at `E:\code\Minecraft\.pb-src\pb-121` (`common/block/entity/` machines, `util/BeeHelper.getBeeProduce`, `common/item/{BeeCage,Gene,HoneyTreat}`); ProductiveLib via `javap` on the jar in `libs/`.

- add-source: Gregification.addSource: a source registers its types() supplier
- machine-type: ForeignMachineType: one gregified machine type (slots, proxies, shape...)
- converter-single: ForeignRecipeConverter.single wraps a Single converter
- mi-tables: MI's per-type tables (SINGLE_BLOCKS, MULTIBLOCKS follows)
- constructor-hook: example of the config + ModList check that adds a source in the GTMQoL constructor

Code:
- src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java
- src/main/java/com/yiran/minecraft/gtmqol/gregification/ForeignMachineType.java
- src/main/java/com/yiran/minecraft/gtmqol/gregification/ForeignRecipeConverter.java
- src/main/java/com/yiran/minecraft/gtmqol/gregification/Gregification.java
- src/main/java/com/yiran/minecraft/gtmqol/gregification/mi/MIGregification.java
