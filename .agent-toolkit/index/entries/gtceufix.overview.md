+++
title = "Upstream bug workarounds in core/mixins/gtceufix: rules, which exist on 1.20.1 vs 1.21.1, recheck after bumps"
keys = [ "both branches", "modularui bug", "remove once fixed" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"
+++
Mixins in `core/mixins/gtceufix/` work around bugs in GTCEu or ModularUI. Each class javadoc says what it fixes and ends with "Remove once ... fixes it": delete it when upstream does (check on every GTCEu / MUI bump, see build.gtceu-upgrade). All of them are listed in `gtmqol.mixins.json` (no separate config on 1.20.1).

On 1.20.1 (checked 2026-10-20), all also on 1.21.1:
- common: `AbstractWidgetMenuMixin`, `EnergyContainerListMixin`, `FluidRecipeCapabilityMixin`, `RecipeLogicMixin`, `RecipeRunnerMixin`
- client: `GTEmiRecipeMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`
AbstractWidgetMenu, BaseSchemaRenderer, DummyChunkSource, EnergyContainerList and RecipeLogic are byte-identical to 1.21.1 apart from imports; FluidRecipeCapability, GTEmiRecipe and RecipeRunner use NBT / GTCEu 1.20 ingredients where 1.21.1 uses data components / NeoForge sized ingredients.

1.21.1 only (bug absent on 1.20.1's GTCEu / MUI / JEI setup): AABB highlight color and see-through, BloomSafeMode, EmiCallWrapper, GameData (duplicate recipe_type RegisterEvent), GTRecipeBuilder (fluid output components), GTRecipeCategory lang, ItemDataComponentMapIngredient, MEPatternBufferInternalSlot, AEConfigSyncHandler, PotionFluidType, JEI RecipeSlot (own config). If one of these symptoms shows up on 1.20.1, port the fix from 1.21.1 rather than assuming it's absent.

Deleted after the 2026-10-14 GTCEu bump (fixed upstream, GTCEu #5540 ghost circuit into empty circuit slot, #5541 custom recipe logic copied into the type): GTMuiWidgetsMixin, GTRecipeTypeMixin.

`CannerLogicMixin` (canner potion representative recipes) is a feature, not a fix, and lives outside `gtceufix/`.

Per-fix entries: gtceufix.*.