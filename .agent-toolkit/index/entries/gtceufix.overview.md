+++
title = "Upstream bug workarounds in core/mixins/gtceufix: rules, which exist on which branch, recheck after bumps"
keys = [ "1.20.1", "both branches", "modularui bug", "remove once fixed" ]
updated = "2026-11-08 19:26:29"
checked = "2026-11-08"

[[items]]
name = "folder"
value = "the gtceufix mixins folder (the 1.21 list above); stale when a fix is added or removed"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix"
dir = [ "AABBHighlightColorMixin.java", "AABBHighlightSeeThroughMixin.java", "AEConfigSyncHandlerMixin.java", "AbstractWidgetMenuMixin.java", "BaseSchemaRendererMixin.java", "BloomSafeModeMixin.java", "DummyChunkSourceMixin.java", "EmiCallWrapperMixin.java", "EnergyContainerListMixin.java", "FluidRecipeCapabilityMixin.java", "GTEmiRecipeMixin.java", "GTRecipeBuilderMixin.java", "GTRecipeCategoryMixin.java", "GameDataMixin.java", "ItemDataComponentMapIngredientMixin.java", "MEPatternBufferInternalSlotMixin.java", "MachineModelVariantOrderMixin.java", "PotionFluidTypeMixin.java", "RecipeLogicMixin.java", "RecipeRunnerMixin.java", "jei/" ]

[[items]]
name = "jei-config"
value = "own mixin config for jei/JeiRecipeSlotMixin"
path = "src/main/resources/gtmqol.jeifix.mixins.json"
snippet = "\"package\": \"com.yiran.minecraft.gtmqol.core.mixins.gtceufix.jei\","
+++
Mixins in `core/mixins/gtceufix/` work around bugs in GTCEu, ModularUI, JEI or NeoForge interplay. Each class javadoc says what it fixes and ends with "Remove once ... fixes it": delete it when upstream does (check on every GTCEu / MUI bump, see skill.gtceu-upgrade).

Branch membership (checked 2026-10-20):
- On **both** branches: `AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`, `EnergyContainerListMixin`, `FluidRecipeCapabilityMixin`, `GTEmiRecipeMixin` (1.20.1 keeps NBT instead of components), `MachineModelVariantOrderMixin`, `RecipeLogicMixin`, `RecipeRunnerMixin` (the 1.20.1 one merges GTCEu 1.20's `SizedIngredient`/`FluidIngredient`, the 1.21 one NeoForge's sized ingredients).
- **1.21 only** (bugs absent in 1.20.1's GTCEu or MUI/JEI setup): `AABBHighlightColorMixin`, `AABBHighlightSeeThroughMixin`, `BloomSafeModeMixin`, `EmiCallWrapperMixin`, `GameDataMixin`, `GTRecipeBuilderMixin`, `GTRecipeCategoryMixin`, `ItemDataComponentMapIngredientMixin`, `MEPatternBufferInternalSlotMixin`, `AEConfigSyncHandlerMixin`, `PotionFluidTypeMixin`, `jei/JeiRecipeSlotMixin` (own config `gtmqol.jeifix.mixins.json`).
- Deleted after the 2026-10-14 GTCEu bump (fixed upstream): `GTMuiWidgetsMixin` (empty circuit slot couldn't take a ghost circuit, GTCEu #5540) and `GTRecipeTypeMixin` (custom recipe logic not copied into the type, #5541).

Per-fix entries: gtceufix.*.