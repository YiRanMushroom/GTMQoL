+++
title = "1.20.1 vs 1.21.1 branch: code differences to keep in mind when cherry-picking"
keys = [ "forge", "neoforge", "port" ]
updated = "2026-10-21 01:13:03"
+++
Two long-lived branches: `1.20` (this one, Forge 47 / MC 1.20.1, worktree `GTMQoL-1.20.1`) and `1.21` (NeoForge 21.1, the default branch). Synced by cherry-picking; 1.21.1 was branched after `87a49de` (GTNL steam machines). Most commits exist on both with different hashes (e.g. 1.20.1 `e882d08` = 1.21.1 `146115b`). Build: both use ModDevGradle now (1.20.1 `legacyForge` since `3b63374`, still with `mod*` configurations, mixin configs declared in build.gradle `mixin { config ... }`; 1.21.1 lists them in neoforge.mods.toml). Build details: build.environment, build.dependencies.

Registration
- 1.20.1: GTCEu posts `GTCEuAPI.RegisterEvent`s on our mod bus; recipe capabilities, recipe types, covers and machines register in `GTMQoL.onRegister*` generic listeners. `IGTAddon.initializeAddon()` runs after the machine registry is frozen, so it is empty. Fields are plain `MachineDefinition` / `GTRecipeType` / `CoverDefinition`; builders are `api.registry.registrate.MachineBuilder` (no `builder` subpackage), `register()` returns the definition. `MachineBuilderMixin` hands every registered definition to `ModularMachines.register` directly.
- 1.21.1: everything is deferred Registrate entries (`MachineEntry`, `GTRecipeTypeEntry`, `Holder<CoverDefinition>`), declared in the constructor; `GTRecipeTypes.X.get()` where 1.20.1 has `GTRecipeTypes.X` (e.g. `WorkableMultiblockMachineMixin`'s lathe check), and mixin targets use `registrate/builder/` class names.
- Creative tab: 1.20.1 registers its own `MAIN_TAB` with `defaultCreativeTab("main", ...)`; 1.21.1 instead nulls the default tab to avoid a NeoForge duplicate-entry error.
- Recipes: 1.20.1 `addRecipes(Consumer<FinishedRecipe>)`, 1.21.1 `RecipeOutput`.

Data / APIs
- Item/fluid data: 1.20.1 NBT (`FluidStack.getTag()`, data stick binding in NBT, `FluidIngredient.getNbt()`); 1.21.1 data components.
- GTCEu ingredients: 1.20.1 GTCEu's own `SizedIngredient` / `FluidIngredient` / `IntProviderFluidIngredient`; 1.21.1 NeoForge's sized ingredients.
- Tags: 1.20.1 `forge:` (CircuitTags `forge:circuits/<tier>`, `forge:alloys/advanced`), 1.21.1 `c:`.
- FE input: 1.20.1 `FEInputProvider` attaches `ForgeCapabilities.ENERGY` via `AttachCapabilitiesEvent<BlockEntity>`; 1.21.1 uses `RegisterCapabilitiesEvent`.
- Events: Forge 47 `TickEvent.ServerTickEvent` with `phase == END` (AutoBuilder, WirelessNetworks), `ServerStoppedEvent`, `ForgeEventFactory.onBlockPlace`.
- Auto-build `StructurePlanner`: 1.20.1 keeps an `IdentityHashMap` node -> root from `getPredicates()` (no `MultiPredicate.getParent()`); 1.21.1 walks `children()`.
- Mixin configs: `compatibilityLevel` JAVA_17, `mixinextras.minVersion` 0.5.5 (MixinExtras is jarJar'd by us; 1.21.1 uses NeoForge's 0.5.3). pack_format 15. mods.toml (not neoforge.mods.toml).
- Mixins only on 1.20.1: JEI category mixins (`GTRecipeJEICategory*`), `EncodePatternTransferHandlerMixin` (JEI encoding), recipedb accessors for pattern buffers, `mekanism.TaggedChemicalStackIngredientAccessor`. Only on 1.21.1: the 1.21-only gtceufix mixins (see gtceufix.overview, incl. `PotionFluidTypeMixin`), `MachineBuilderAccessor`. The potion mixins (`BreweryLogicMixin`, `GTRecipeViewerWidgetPotionMixin`, `PotionItemFluidHandlerMixin`, `BottleItemFluidHandlerMixin`, `PotionFluidNameMixin`) are on both.
- `KubeJSDataGenFix` only on 1.20.1 (Forge 47 never sets the datagen flag KubeJS checks), see config.datagen-1.20.1.
- Recipe viewer in dev: 1.20.1 has JEI (pinned 15.20.0.115 with GTCEu) and EMI compile-only; 1.21.1 dev must use EMI.
- Optional mods: Modern Industrialization gregification is 1.21.1 only; Mekanism is 10.4 on 1.20.1.

Mostly written on 1.21.1 first, then ported: gregification, stack-like capabilities, universal ME parts, Jade, potion features, ghost ingredients, block providers, Create gregification.