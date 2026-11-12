+++
title = "Source layout: packages and what lives where (1.21.1)"
keys = [ "source tree" ]
updated = "2026-11-12 06:56:25"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol"
dir = [ "GTMQoL.java", "GTMQoLAddon.java", "RenamedIds.java", "api/", "client/", "common/", "config/", "core/", "data/", "gregification/", "integration/", "package-info.java" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common"
dir = [ "assembler/", "autobuild/", "blockprovider/", "circuit/", "crafter/", "crystal/", "engraving/", "feinput/", "ghost/", "greenhouse/", "implosion/", "modular/", "multiblock/", "overclock/", "potion/", "probable/", "recipedb/", "singleblock/", "stacklike/", "steam/", "test/", "ui/", "wireless/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core"
dir = [ "AE2MixinPlugin.java", "GTMQoLMixinConfigPlugin.java", "GTMQoLMixinPlugin.java", "JeiRecipeSlotFixPlugin.java", "RecipeDBMixinPlugin.java", "mixins/", "package-info.java" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data"
dir = [ "recipe/", "tag/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration"
dir = [ "ae2/", "jade/", "kubejs/", "productivebees/" ]
+++
Loosely follows GTCEu Modern (api / common / data / core / integration / client / config). Rule: the top level holds layers; each feature is one package under common/ (or integration/<mod>/ when it can't exist without that mod) and owns all its code, renders, recipes and tags included. Code of a feature that needs an optional mod goes in a <modid>/ subpackage (common/stacklike/mekanism, common/stacklike/ae2, common/wireless/energy/ae2). Mixins stay in core/mixins (configs need one package each).

src/main/java/com/yiran/minecraft/gtmqol/
- GTMQoL.java: entry point, GTMQoL.id(), what gets registered (config gated), init order
- GTMQoLAddon.java: @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
- RenamedIds.java: old block/item IDs mapped to their new ones, so saved worlds keep the blocks and their contents
- api/: shared building blocks: generation/ (runtime resource generation, opt-in per machine), machine/ (MachineFeatures, api.machine-features)
- client/: client-only setup shared by features (GTMQoLClient, render types, bloom rebuild, Shift-drag); feature renders live in their features
- config/: GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
- common/: assembler (MagicalAssembler + UI), autobuild, blockprovider (block capability), crafter (pattern crafter, crafting pattern item, DecodedPatterns), circuit (UniversalCircuits, ControlCircuits, CircuitTags), crystal, engraving (Advanced Chip Engraving Factory), feinput (FE into EU inputs), ghost, greenhouse (+ Mystical Agriculture recipes), implosion, modular, multiblock (SAF, DTFR, cyclic: void miner, fishing pond, their renders), overclock (+ AbsoluteParallel), potion, probable (probable devices), recipedb (non-mixin side of grouped search), singleblock, stacklike (mekanism/, ae2/), steam (SteamTweaks), test (IntegrationTests; MixinTargetCheck, data runs only), ui (PopupWidgets, RecipeTypeLayouts), wireless (WirelessRecipes; steam/, energy/ (+ ae2/ energy cell), channel/ with computation/ and optical/)
- gregification/: Gregification, ForeignMachineType, GregifiedRecipeType, modifiers, MarkCoveredRecipes, CasingPartRender; mi/, mekanism/, productivebees/, create/, client/
- data/: recipes and tags no feature owns: recipe/ (MiscRecipes, EarlyGameRecipes, LegacyPartRecipes), tag/ (MaterialAliasTags, WireTags)
- core/: mixin config plugins (GTMQoL, RecipeDB, AE2, JeiRecipeSlotFix); mixins/ with gtceufix/, recipedb/, ae2/
- integration/: ae2/ (AE2Machines, AEProcessing, AECrafterPattern; patternbuffer/, parts/, stickycard/, crafting/ planner and pattern batching, matrix/ Assembling Matrix), jade/, kubejs/, productivebees/