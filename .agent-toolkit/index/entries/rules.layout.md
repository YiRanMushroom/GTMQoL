+++
title = "Source layout: packages and what lives where (1.21.1)"
keys = [ "source tree" ]
updated = "2026-10-29 07:35:16"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol"
dir = [ "GTMQoL.java", "GTMQoLAddon.java", "api/", "client/", "common/", "config/", "core/", "data/", "gregification/", "integration/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common"
dir = [ "assembler/", "autobuild/", "blockprovider/", "circuit/", "crystal/", "fe/", "ghost/", "greenhouse/", "implosion/", "modifier/", "modular/", "multiblock/", "overclock/", "part/", "potion/", "probable/", "recipedb/", "singleblock/", "stacklike/", "steam/", "test/", "ui/", "wireless/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core"
dir = [ "AE2MixinPlugin.java", "EAPMixinPlugin.java", "GTMQoLMixinConfigPlugin.java", "GTMQoLMixinPlugin.java", "JeiRecipeSlotFixPlugin.java", "RecipeDBMixinPlugin.java", "mixins/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data"
dir = [ "recipe/", "tag/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration"
dir = [ "ae2/", "jade/", "kubejs/", "mysticalagriculture/", "productivebees/" ]
+++
Loosely follows GTCEu Modern (api / common / data / core / integration / client / config). Features are kept together as subpackages of common/, not split by kind the way GTCEu does.

src/main/java/com/yiran/minecraft/gtmqol/
- GTMQoL.java: entry point, GTMQoL.id(), what gets registered (config gated), init order
- GTMQoLAddon.java: @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
- api/generation/: runtime (dynamic) resource generation, opt-in per machine
- client/: client-only init and renders (DTFR ring, fishing pond water, Create casing parts)
- config/: GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
- common/: assembler (MagicalAssembler + UI), autobuild, blockprovider (block capability), circuit (UniversalCircuits, ControlCircuits), crystal, fe (FEInputProvider), ghost, greenhouse, implosion, modifier (AbsoluteParallel), modular, multiblock (SAF, DTFR, cyclic: void miner, fishing pond), overclock, part (LegacyPartRecipes), potion (PotionContainers), probable (probable devices), recipedb (non-mixin side of grouped search), singleblock (SingleblockFeature, ComposableTieredMachine, BatchModeTrait), stacklike (+ mekanism/ chemicals), steam, test (IntegrationTests), ui (PopupWidgets), wireless (steam/, energy/; channel/ with computation/ and optical/: wireless.channels)
- gregification/: Gregification, ForeignMachineType, GregifiedRecipeType, modifiers, MarkCoveredRecipes; mi/, mekanism/, productivebees/, create/, client/
- data/: recipe/ (MiscRecipes, EarlyGameRecipes, WirelessRecipes), tag/ (CircuitTags, MaterialAliasTags, WireTags)
- core/: mixin config plugins (GTMQoL, RecipeDB, EAP, AE2, JeiRecipeSlotFix); mixins/ with gtceufix/, recipedb/, eap/, ae2/
- integration/: ae2/ (+ stacklike/ bridges), jade/, kubejs/, mysticalagriculture/, productivebees/