+++
title = "Upstream bug workarounds in core/mixins/gtceufix: rules, which exist on which branch, recheck after bumps"
keys = [ "1.20.1", "both branches", "modularui bug", "remove once fixed" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-29"
+++
Mixins in `core/mixins/gtceufix/` work around bugs in GTCEu, ModularUI, JEI or NeoForge interplay. Each class javadoc says what it fixes and ends with "Remove once ... fixes it": delete it when upstream does (check on every GTCEu / MUI bump, see build.gtceu-upgrade).

Branch membership (checked 2026-10-20):
- On **both** branches: `AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`, `EnergyContainerListMixin`, `FluidRecipeCapabilityMixin`, `GTEmiRecipeMixin` (1.20.1 keeps NBT instead of components), `RecipeLogicMixin`, `RecipeRunnerMixin` (the 1.20.1 one merges GTCEu 1.20's `SizedIngredient`/`FluidIngredient`, the 1.21 one NeoForge's sized ingredients).
- **1.21 only** (bugs absent in 1.20.1's GTCEu or MUI/JEI setup): `AABBHighlightColorMixin`, `AABBHighlightSeeThroughMixin`, `BloomSafeModeMixin`, `EmiCallWrapperMixin`, `GameDataMixin`, `GTRecipeBuilderMixin`, `GTRecipeCategoryMixin`, `ItemDataComponentMapIngredientMixin`, `MEPatternBufferInternalSlotMixin`, `AEConfigSyncHandlerMixin`, `PotionFluidTypeMixin`, `jei/JeiRecipeSlotMixin` (own config `gtmqol.jeifix.mixins.json`).
- Deleted after the 2026-10-14 GTCEu bump (fixed upstream): `GTMuiWidgetsMixin` (empty circuit slot couldn't take a ghost circuit, GTCEu #5540) and `GTRecipeTypeMixin` (custom recipe logic not copied into the type, #5541).
- The canner potion representative recipes (`CannerLogicMixin`) are a feature, not a fix, and live outside `gtceufix/` (both branches).

Per-fix entries: gtceufix.*.