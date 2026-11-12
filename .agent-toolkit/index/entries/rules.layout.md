+++
title = "Source layout: package tree of com.yiran.minecraft.gtmqol (1.20.1) and where resources live"
keys = [ "packages", "src/generated/resources", "where is" ]
updated = "2026-11-12 06:56:32"
checked = "2026-11-12"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol"
dir = [ "GTMQoL.java", "GTMQoLAddon.java", "RenamedIds.java", "api/", "client/", "common/", "config/", "core/", "data/", "gregification/", "integration/", "package-info.java" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common"
dir = [ "assembler/", "autobuild/", "blockprovider/", "circuit/", "crafter/", "crystal/", "engraving/", "feinput/", "ghost/", "greenhouse/", "implosion/", "modular/", "multiblock/", "overclock/", "potion/", "probable/", "recipedb/", "singleblock/", "stacklike/", "steam/", "test/", "ui/", "wireless/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core"
dir = [ "AE2MixinPlugin.java", "GTMQoLMixinConfigPlugin.java", "GTMQoLMixinPlugin.java", "RecipeDBMixinPlugin.java", "mixins/", "package-info.java" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data"
dir = [ "recipe/", "tag/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration"
dir = [ "ae2/", "jade/", "kubejs/", "productivebees/" ]
+++
Rule (same as 1.21): the top level holds layers; each feature is one package under common/ (or integration/<mod>/ when it can't exist without that mod) and owns all its code, renders, recipes and tags included. Code of a feature that needs an optional mod goes in a <modid>/ subpackage. Mixins stay in core/mixins.

`src/main/java/com/yiran/minecraft/gtmqol/`:

```text
GTMQoL.java          entry point, GTMQoL.id(), GTCEu RegisterEvent listeners (onRegister*)
GTMQoLAddon.java     @GTAddon: GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
RenamedIds.java      old block/item IDs mapped to new ones (MissingMappingsEvent), saved worlds keep them
api/generation/      runtime resource generation (RuntimeGeneration, GTMQoL*Builder, opt-in per machine)
api/machine/         MachineFeatures (api.machine-features)
client/              GTMQoLClient, render types, Shift-drag; feature renders live in their features
common/
  assembler/         magical assembler (type, machines, UI)
  autobuild/         survival auto-build button
  blockprovider/     gtmqol:block recipe capability
  circuit/           UniversalCircuits, ControlCircuits, CircuitTags
  crafter/           pattern crafter, crafting pattern items, DecodedPatterns
  crystal/ engraving/ greenhouse/ (+ Mystical Agriculture recipes) implosion/   single-feature multiblocks
  feinput/           FEInputProvider (FE into every EU input)
  ghost/             ghost ingredient capability, hatch, marks
  singleblock/       composable single blocks (SingleblockFeature, BatchModeTrait)
  modular/           modular multiblocks of single blocks
  multiblock/        GTMQoLMultiblocks: SAF, DTFR, cyclic (void miner, fishing pond), their renders
  overclock/ (+ AbsoluteParallel) potion/ recipedb/ (non-mixin side) stacklike/ (mekanism/, ae2/)
  probable/          probable certainty / improbability devices
  steam/             advanced steam multis, steam parallel hatch, SteamTweaks
  test/              IntegrationTests; MixinTargetCheck (data runs only)
  ui/                PopupWidgets (shared popup look, counter rows), RecipeTypeLayouts
  wireless/          networks, binding, covers, WirelessRecipes; steam/ and energy/ (+ ae2/ energy cell); channel/ (computation/, optical/)
config/              GTMQoLConfig (toma), EarlyConfig (gtmqol-early.properties)
core/                mixin plugins (GTMQoL, RecipeDB, AE2)
  mixins/            feature mixins; ae2/ mekanism/ recipedb/ gtceufix/ (upstream bug workarounds)
data/recipe/ data/tag/   recipes and tags no feature owns: MiscRecipes, EarlyGameRecipes, LegacyPartRecipes; MaterialAliasTags, WireTags
gregification/       foreign recipe types as GT machines, CasingPartRender; create/ mekanism/ productivebees/ client/
integration/         ae2/ (AE2Machines, AEProcessing, AECrafterPattern; patternbuffer/, parts/, stickycard/, crafting/ planner and pattern batching, matrix/ Assembling Matrix), jade/, kubejs/, productivebees/
```

Datagen output is committed in `src/generated/resources` (on the main resources source set); static textures in `src/main/resources/assets/gtmqol/`.