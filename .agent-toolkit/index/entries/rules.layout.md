+++
title = "Source layout: packages and what lives where (1.21.1)"
keys = [ "source tree" ]
updated = "2026-10-19 20:28:07"
+++
Loosely follows GTCEu Modern (api / common / data / core / integration / client / config). Features are kept together as subpackages of common/, not split by kind the way GTCEu does.

src/main/java/com/yiran/minecraft/gtmqol/
- GTMQoL.java: entry point, GTMQoL.id(), what gets registered (config gated), init order
- GTMQoLAddon.java: @GTAddon, owns the GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
- api/generation/: runtime (dynamic) resource generation, opt-in per machine
- client/: client-only init and renders (DTFR ring, fishing pond water, Create casing parts)
- config/: GTMQoLConfig (toma, runtime), EarlyConfig (properties, mixin time)
- common/: assembler (MagicalAssembler + UI), autobuild, blockprovider (block capability), circuit (UniversalCircuits, ControlCircuits), crystal, fe (FEInputProvider), ghost, greenhouse, implosion, modular, multiblock (SAF, DTFR, cyclic: void miner, fishing pond), overclock, part (LegacyPartRecipes), potion (PotionContainers), recipedb (non-mixin side of grouped search), singleblock (SingleblockFeature, ComposableTieredMachine, BatchModeTrait), stacklike (+ mekanism/ chemicals), steam, test (IntegrationTests), ui (PopupWidgets), wireless (steam/, energy/)
- gregification/: Gregification, ForeignMachineType, GregifiedRecipeType, modifiers, MarkCoveredRecipes; mi/, mekanism/, productivebees/, create/, client/
- data/: recipe/ (MiscRecipes, EarlyGameRecipes, WirelessRecipes), tag/ (CircuitTags, MaterialAliasTags, WireTags)
- core/: mixin config plugins (GTMQoL, RecipeDB, EAP, AE2, JeiRecipeSlotFix); mixins/ with gtceufix/, recipedb/, eap/, ae2/
- integration/: ae2/ (+ stacklike/ bridges), jade/, mysticalagriculture/, productivebees/