+++
title = "Source layout: package tree of com.yiran.minecraft.gtmqol (1.20.1) and where resources live"
keys = [ "packages", "src/generated/resources", "where is" ]
updated = "2026-10-29 07:34:16"
checked = "2026-10-29"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol"
dir = [ "GTMQoL.java", "GTMQoLAddon.java", "api/", "client/", "common/", "config/", "core/", "data/", "gregification/", "integration/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common"
dir = [ "assembler/", "autobuild/", "blockprovider/", "circuit/", "crystal/", "fe/", "ghost/", "greenhouse/", "implosion/", "modifier/", "modular/", "multiblock/", "overclock/", "part/", "potion/", "probable/", "recipedb/", "singleblock/", "stacklike/", "steam/", "test/", "ui/", "wireless/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core"
dir = [ "AE2MixinPlugin.java", "EAPMixinPlugin.java", "GTMQoLMixinConfigPlugin.java", "GTMQoLMixinPlugin.java", "RecipeDBMixinPlugin.java", "mixins/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/data"
dir = [ "recipe/", "tag/" ]

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration"
dir = [ "KubeJSDataGenFix.java", "ae2/", "jade/", "kubejs/", "mysticalagriculture/", "productivebees/" ]
+++
`src/main/java/com/yiran/minecraft/gtmqol/`:

```text
GTMQoL.java          entry point, GTMQoL.id(), GTCEu RegisterEvent listeners (onRegister*)
GTMQoLAddon.java     @GTAddon: GTRegistrate, creative tab, machine()/multiblock() helpers, addRecipes
api/generation/      runtime resource generation (RuntimeGeneration, GTMQoL*Builder, opt-in per machine)
client/              GTMQoLClient, renders (DTFR ring, fishing pond water, Create casing parts)
common/
  assembler/         magical assembler (type, machines, UI)
  autobuild/         survival auto-build button
  blockprovider/     gtmqol:block recipe capability
  circuit/           UniversalCircuits, ControlCircuits
  crystal/ greenhouse/ implosion/   single-feature multiblocks
  fe/                FEInputProvider (FE into every EU input)
  ghost/             ghost ingredient capability, hatch, marks
  modifier/ singleblock/  shared recipe modifiers, composable single blocks
  modular/           modular multiblocks of single blocks
  multiblock/        GTMQoLMultiblocks: SAF, DTFR, cyclic (void miner, fishing pond)
  overclock/ part/ potion/ recipedb/ (non-mixin side) stacklike/ (+mekanism/)
  probable/          probable certainty / improbability devices
  steam/             advanced steam multis, steam parallel hatch
  test/              IntegrationTests
  ui/                PopupWidgets (shared popup look, counter rows)
  wireless/          networks, binding, covers; steam/ and energy/ per resource; channel/ (computation/, optical/)
config/              GTMQoLConfig (toma), EarlyConfig (gtmqol-early.properties)
core/                mixin plugins (GTMQoL, RecipeDB, AE2, EAP)
  mixins/            feature mixins; ae2/ eap/ mekanism/ recipedb/ gtceufix/ (upstream bug workarounds)
data/recipe/ data/tag/   MiscRecipes, EarlyGameRecipes, WirelessRecipes; CircuitTags, MaterialAliasTags, WireTags
gregification/       foreign recipe types as GT machines; create/ mekanism/ productivebees/ client/
integration/         KubeJSDataGenFix; ae2/ (+stacklike/), jade/, kubejs/, mysticalagriculture/, productivebees/
```

Datagen output is committed in `src/generated/resources` (on the main resources source set); static textures in `src/main/resources/assets/gtmqol/`.