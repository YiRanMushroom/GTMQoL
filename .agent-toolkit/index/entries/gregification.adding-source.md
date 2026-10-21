+++
title = "How to add a gregification type or a source for a new mod, and where to look in upstream sources"
keys = [ "gregification core" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"
+++
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