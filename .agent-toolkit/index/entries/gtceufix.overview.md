+++
title = "Upstream bug workarounds in core/mixins/gtceufix: rules, which exist on 1.20.1 vs 1.21.1, recheck after bumps"
keys = [ "both branches", "modularui bug", "remove once fixed" ]
updated = "2026-11-07 09:54:35"
checked = "2026-11-07"

[[items]]
name = "package"
value = "The gtceufix mixins (directory link: stale when one is added or removed)"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix"
dir = [ "AbstractWidgetMenuMixin.java", "BaseSchemaRendererMixin.java", "DummyChunkSourceMixin.java", "EnergyContainerListMixin.java", "FluidRecipeCapabilityMixin.java", "GTEmiRecipeMixin.java", "MachineModelVariantOrderMixin.java", "RecipeLogicMixin.java", "RecipeRunnerMixin.java" ]

[[items]]
name = "mixin-config-common"
value = "Common gtceufix mixins in gtmqol.mixins.json"
path = "src/main/resources/gtmqol.mixins.json"
snippet = "\"gtceufix.RecipeRunnerMixin\""

[[items]]
name = "mixin-config-client"
value = "Client gtceufix mixins in gtmqol.mixins.json"
path = "src/main/resources/gtmqol.mixins.json"
snippet = "\"gtceufix.DummyChunkSourceMixin\""

[[items]]
name = "canner-feature"
value = "Feature mixin kept outside gtceufix"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/CannerLogicMixin.java"
snippet = "public class CannerLogicMixin {"
+++
Mixins in `core/mixins/gtceufix/` work around bugs in GTCEu or ModularUI. Each class javadoc says what it fixes and ends with "Remove once ... fixes it": delete it when upstream does (check on every GTCEu / MUI bump, see skill.gtceu-upgrade). All of them are listed in `gtmqol.mixins.json` (no separate config on 1.20.1).

On 1.20.1 (checked 2026-10-29 against GTCEu 327 / MUI 25: none of their targets changed upstream), all also on 1.21.1:
- common: `AbstractWidgetMenuMixin`, `EnergyContainerListMixin`, `FluidRecipeCapabilityMixin`, `RecipeLogicMixin`, `RecipeRunnerMixin`, `MachineModelVariantOrderMixin` (only matters in data generation)
- client: `GTEmiRecipeMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`
AbstractWidgetMenu, BaseSchemaRenderer, EnergyContainerList and RecipeLogic are byte-identical to 1.21.1 apart from imports; DummyChunkSource lacks 1.21.1's clear() wrapper (1.20.1's MUI has no clear); FluidRecipeCapability, GTEmiRecipe and RecipeRunner use NBT / GTCEu 1.20 ingredients where 1.21.1 uses data components / NeoForge sized ingredients.

1.21.1 only (bug absent on 1.20.1's GTCEu / MUI / JEI setup): AABB highlight color and see-through, BloomSafeMode, EmiCallWrapper, GameData (duplicate recipe_type RegisterEvent), GTRecipeBuilder (fluid output components), GTRecipeCategory lang, ItemDataComponentMapIngredient, MEPatternBufferInternalSlot, AEConfigSyncHandler, PotionFluidType, JEI RecipeSlot (own config). If one of these symptoms shows up on 1.20.1, port the fix from 1.21.1 rather than assuming it's absent.

Deleted after the 2026-10-14 GTCEu bump (fixed upstream, GTCEu #5540 ghost circuit into empty circuit slot, #5541 custom recipe logic copied into the type): GTMuiWidgetsMixin, GTRecipeTypeMixin.

`CannerLogicMixin` (canner potion representative recipes) is a feature, not a fix, and lives outside `gtceufix/`.

Per-fix entries: gtceufix.*.