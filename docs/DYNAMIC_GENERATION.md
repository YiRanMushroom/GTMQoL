# Runtime resource and data generation

This branch starts from an empty Java implementation. The previous implementation is archived under `reference/` and is not part of the active source set.

The goal is to create machine content without running Gradle resource/data generation and without relying on `src/generated/resources`.

## Boundary

Runtime generation does not replace registry registration:

```text
normal mod initialization
    -> register item/block/machine/recipe type
    -> publish runtime client resources
    -> publish runtime server data
```

Items, blocks, and machine definitions must still be registered during the normal Forge/GTCEu lifecycle. The runtime generation layer only publishes their JSON representation.

## Client resources

GTCEu provides `GTDynamicResourcePack`. It is mounted by GTCEu during `AddPackFindersEvent` and is cleared before resource reload. New code should write to it through `RuntimeGeneration`.

Supported resource categories:

- machine and item models;
- blockstates;
- language JSON;
- arbitrary client JSON under `assets/<namespace>/...`.

The path passed to `addModel` is interpreted as a model identifier. The path passed to `addBlockState` is interpreted as a blockstate identifier. For arbitrary files, pass the complete resource path to `addClientResource`.

### Single-block machine

A single-block machine needs:

1. a registered `MachineDefinition`;
2. a block model;
3. a blockstate pointing to that model;
4. an item model, usually delegating to the block model;
5. language entries for the block and item;
6. recipe JSON supplied through the GTCEu addon recipe provider.

The machine class and recipe type provide behavior. They do not need a generated data file unless the specific behavior requires one.

### Multiblock machine

A multiblock machine's structure is defined in Java with `FactoryBlockPattern`; the pattern itself is not a resource-generation output. Runtime resources still need to provide:

- the controller block model and blockstate;
- the controller item model;
- the casing/overlay machine model;
- language and tooltip entries;
- recipes.

The first implementation should provide adapters that turn a registered `MachineBuilder` into these resources, rather than copying generated files into the repository.

## Server data

GTCEu's `IGTAddon.addRecipes(Consumer<FinishedRecipe>)` is the high-level entry point for recipes. `GTMQoLAddon` owns that callback and exposes it through `RuntimeGeneration.addRecipe`.

The next server-side API to add is a generic JSON writer for tags, advancements, and other data paths. It should target GTCEu's `GTDynamicDataPack` and use one central collision policy:

- same path and same JSON: accept;
- same path and different JSON: log an error and fail fast;
- clear all runtime data on reload through GTCEu's existing pack lifecycle.

## Current starting point

The active source tree currently contains only:

- `GTMQoL`: the Forge mod entry point;
- `GTMQoLAddon`: the GTCEu addon and recipe callback;
- `RuntimeGeneration`: the first client resource and recipe API.

The next implementation step is a minimal test machine with all client JSON emitted through `RuntimeGeneration`, followed by a generic server-data writer and a multiblock adapter.
