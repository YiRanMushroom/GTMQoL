+++
title = "1.21.1 vs 1.20.1 branch: code differences to keep in mind when cherry-picking"
keys = [ "forge", "neoforge", "port" ]
updated = "2026-11-11 07:18:17"
checked = "2026-11-11"

[[items]]
name = "constructor"
value = "1.21: all content declared in the GTMQoL constructor, recipe types before their machines"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "public GTMQoL(IEventBus modBus) {"

[[items]]
name = "modular-queue"
value = "1.21 MachineBuilderMixin only queues modular machine entries"
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "public abstract class MachineBuilderMixin implements IDynamicGenerationHandler {"

[[items]]
name = "modular-register"
value = "1.21 modular multiblocks declared in the gtceu:machine RegisterEvent at NORMAL priority"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "if (event.getRegistryKey().equals(GTRegistries.Keys.MACHINE)) {"

[[items]]
name = "creative-tab"
value = "1.21 null default creative tab"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);"

[[items]]
name = "autobuild-roots"
value = "1.21 auto-build predicate roots from children()"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/StructurePlanner.java"
snippet = "private static Map<MultiPredicate, MultiPredicate> roots(IBlockPattern pattern) {"

[[items]]
name = "mixinextras"
value = "1.21 mixinextras minVersion 0.5.3 (same in every gtmqol.*.mixins.json)"
path = "src/main/resources/gtmqol.mixins.json"
snippet = "\"minVersion\": \"0.5.3\""
+++
Two long-lived branches: `1.21` (NeoForge 21.1, the default branch) and `1.20` (Forge 1.20.1, worktree `GTMQoL-1.20.1` next to the 1.21 one). Kept in sync by cherry-picking; 1.21 was branched from `87a49de` (GTNL steam machines). A multi-version tool (Stonecutter) is only an idea, not agreed. Build setup differences: build.environment, build.dependencies.

Registration
- 1.21: GTCEu registers everything as deferred Registrate entries. Machine/cover/recipe type fields are `MachineEntry` / `Holder<CoverDefinition>` / `GTRecipeTypeEntry`. All content is declared in the `GTMQoL` constructor, each recipe type before the machines using it (the magical assembler's first). `IGTAddon` has no `initializeAddon`. Builders are in `api.registry.registrate.builder`; `register()` returns `MachineEntry`, properties via `properties()`. `RuntimeGeneration` keeps builder and entry.
- 1.20.1: machines must register in `GTMQoL.onRegisterMachines` (GTCEu's machine `RegisterEvent`); `IGTAddon.initializeAddon()` runs after the registry is frozen.
- 1.21 modular machines: `MachineBuilderMixin` only queues the entry plus the builder's recipe type suppliers (`MachineBuilderMixin`); the multiblocks are declared in the `gtceu:machine` `RegisterEvent` at NORMAL priority, before our registrate's LOW listener and after recipe types exist (config is readable then).
- 1.21 `GTMQoLAddon` calls `defaultCreativeTab((ResourceKey) null)` (see build.creative-tab).

Data / APIs
- Data stick binding: 1.21 `DataComponents.CUSTOM_DATA` / `CUSTOM_NAME`; 1.20.1 NBT. SavedData: 1.21 `SavedData.Factory` + `HolderLookup.Provider`.
- `CircuitTags`: 1.21 uses `c:` tags (Mekanism 1.21: `c:circuits/<tier>`, `c:alloys/advanced`); 1.20.1 `forge:`.
- FE input: 1.21 `FEInputProvider` registers `Capabilities.EnergyStorage.BLOCK` in `RegisterCapabilitiesEvent`; GTCEu registers first so its own FE storages win.
- Mixin configs: `mixinextras.minVersion` 0.5.3 on 1.21 (bundled with NeoForge 21.1.248), 0.5.5 on 1.20.1. 1.21 `compatibilityLevel` JAVA_21, pack_format 34.
- `KubeJSDataGenFix` exists only on 1.20.1 (NeoForge's `DatagenModLoader` sets the datagen flag correctly).
- Auto-build (`common/autobuild/`): 1.20.1 uses `FriendlyByteBuf` (blocks via `writeId`/`readById` on `BuiltInRegistries.BLOCK`) and Forge 47 events (`TickEvent.ServerTickEvent` END phase, `ServerStoppedEvent`, `ForgeEventFactory.onBlockPlace`). Predicate roots: neither branch's pinned GTCEu has `MultiPredicate.getParent()` any more (the unpacked source folders may be older, skill.gtceu-upgrade); 1.21 `StructurePlanner.roots` walks `children()` from `getPredicates()` / `ExpandablePattern.getSymbolMap()`; 1.20.1 builds an `IdentityHashMap` node -> root from `getPredicates()` for block patterns, and for expandable patterns does one extra plain `populate` pass and asks `getPredicateFromPos` for every position.
- AE2 power units: 1.21 `PowerUnit`, 1.20.1 `PowerUnits`. `DynamicRenderType` takes a `MapCodec` on 1.21, a `Codec` on 1.20.1. Level.invalidateCapabilities is 1.21 only. FaceLayer is 1.21 only: 1.20.1 overlays stack by box size (OUTPUT_OVERLAY, AUTO_OUTPUT_OVERLAY).
- Bug workarounds differ per branch: see gtceufix.overview.

Written on 1.21 first, then ported: gregification, stack-like capabilities, universal ME parts, Jade, potion features. Current sync state is tracked in context.md.