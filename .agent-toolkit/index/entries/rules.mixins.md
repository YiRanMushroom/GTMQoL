+++
title = "Mixin conventions: packages, configs and their plugins, early switches, accessors for private types, lambda targets"
updated = "2026-10-19 20:12:38"
+++
- Mixin classes live in core/mixins/ (ae2/, eap/, recipedb/; workarounds for GTCEu/MUI bugs in gtceufix/, deleted once upstream fixes them, see gtceufix.overview). Only mixin classes in a config's package; helpers go elsewhere (e.g. common/recipedb/), else loading fails.
- Plain mixins: gtmqol.mixins.json. Its plugin GTMQoLMixinPlugin skips mekanism/ mixins without Mek and the overclocking.overhaul ones by class name, so anything else added to those OC mixins disappears with the overhaul off.
- A set that must switch on/off as a whole gets its own config whose plugin decides in shouldApplyMixin (gtmqol.recipedb/eap/ae2.mixins.json). Plugins extend GTMQoLMixinConfigPlugin (no-op defaults, isModLoading for mods not constructed yet). Register new configs in build.gradle `mixin { config ... }` (it also makes the refmap; 1.21: neoforge.mods.toml).
- Mixin configs load before any mod is constructed, so GTMQoLConfig can't be read there. Startup switches go in config/gtmqol-early.properties via java.util.Properties (EarlyConfig, like GTCEu's gtceu-early.properties). Changes that can't be undone at runtime (@Overwrite etc.) get such a switch or none.
- Private/package-private types: @Accessor matches the field descriptor, so the return type can't be Object in place of an invisible type; the target object can be cast to Object then to the accessor interface (@Mixin(targets = "...$Inner")).
- Lambda targets: javac's synthetic name (lambda$getNext$0) or a regex (/^lambda\$/); recheck after GTCEu bumps.