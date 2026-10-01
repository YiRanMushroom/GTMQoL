# GTMQoL Current Context

## User goal

This project is being restarted as a clean Java-only implementation for Minecraft 1.20.1 with GTCEu v8. The old implementation is reference material only. Do not migrate old features wholesale and do not reintroduce Kotlin.

The immediate goal is to make runtime resource/data generation work without running Gradle resource generation and without using `src/generated/resources`. The user wants to enable an integration option, launch the client, and inspect real generated content in-game:

- one registered single-block machine;
- one registered multiblock controller;
- assets generated through GTCEu's builders and existing GTCEu textures/models;
- no hand-written test JSON and no fake Stone-to-Dirt recipe.

The user asked for `runClient` to work. The next agent should prioritize build correctness and client startup over expanding features.

## Repository state

- Repository: `YiRanMushroom/GTMQoL`
- Current branch: `migrate/gtceu-v8-runtime-generation`
- Old implementation is archived under `reference/`:
  - `reference/src/main/java`
  - `reference/src/main/kotlin`
  - `reference/src/main/resources`
  - `reference/src/generated`
- Active source should remain only under `src/main/java`.
- Do not delete or modify `reference/` unless explicitly needed for research.
- Current work is uncommitted. Preserve unrelated/user changes.

## Target versions

- Minecraft `1.20.1`
- Forge `47.4.1`
- GTCEu `8.0.0-SNAPSHOT`
- Java language target `17`
- Architectury Loom `1.13.467`

The machine currently has IntelliJ IDEA bundled JBR at:

```text
C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr
```

It is Java 25, not Java 17. It can start Gradle configuration, but compatibility must be considered.

## Current active source layout

```text
src/main/java/com/yiran/minecraft/gtmqol/
├── GTMQoL.java
├── GTMQoLAddon.java
├── config/
│   └── GTMQoLConfig.java
├── generation/
│   └── RuntimeGeneration.java
└── integration/
    └── IntegrationTests.java
```

## Important current code behavior

### `GTMQoL.java`

The Forge entry point now receives `FMLJavaModLoadingContext` in its constructor rather than calling the deprecated static `ModLoadingContext.get()`:

```java
public GTMQoL(FMLJavaModLoadingContext context) {
    IEventBus modBus = context.getModEventBus();
    context.registerConfig(ModConfig.Type.COMMON, GTMQoLConfig.SPEC);
    modBus.addListener(this::onClientSetup);
}
```

This change was made because Forge reports:

```java
@Deprecated(forRemoval = true, since = "1.21.1")
public static ModLoadingContext get()
```

The next agent should verify the exact Forge 47.4.1 API and add a logger if needed. `GTMQoL.LOGGER` is currently referenced by `IntegrationTests`, but `GTMQoL.java` currently does not define it; this is a known compile issue.

### `GTMQoLAddon.java`

`GTMQoLAddon` implements `IGTAddon`, owns the `GTRegistrate`, and calls `IntegrationTests.registerExampleMachines()` during `initializeAddon()` when the config is enabled.

### `GTMQoLConfig.java`

The only active config option is:

```toml
[integration_tests]
enabled = false
```

File at runtime:

```text
config/gtmqol-common.toml
```

When enabled, the intended behavior is visible test content, not a conventional unit/integration test.

### `RuntimeGeneration.java`

This is intended to wrap GTCEu's `GTDynamicResourcePack` and the runtime blockstate provider. It currently contains:

- recipe provider helpers left over from the earlier design;
- `addClientResource`, `addModel`, `addBlockState`, `addLanguage`;
- an experimental `generateMachineAssets(MachineDefinition)` method.

The experimental method currently creates a `DataGenContext`, uses `RuntimeBlockstateProvider.INSTANCE`, applies a basic GT machine model based on the existing GTCEu `block/machine/ev_assembler` model, creates an item model parent, and calls `provider.run()`.

This code has not successfully compiled yet. The next agent should simplify and correct it using the actual GTCEu v8 classes, preferably reusing GTCEu's existing `MachineBuilder` asset generation API instead of duplicating it.

### `IntegrationTests.java`

This class attempts to register:

```text
gtmqol:runtime_single_block
gtmqol:runtime_multiblock
```

The single block uses `SimpleTieredMachine`. The multiblock uses `WorkableElectricMultiblockMachine`, `MultiblockPatternBuilder`/the v8 pattern API, a Stone casing predicate, and a small 3x3x3 structure.

The current file was written against partially inferred APIs and requires compile correction.

## Build configuration

### `settings.gradle`

Uses:

- Architectury Maven
- Forge Maven
- Fabric Maven
- Gradle Plugin Portal
- Maven Central

Fabric Maven was added because Architectury Loom's plugin dependencies include `stitch`, `tiny-remapper`, `class-tweaker`, `lorenz-tiny`, `fabric-loom-native`, `mercurymixin`, and `unpick`.

### `build.gradle`

Uses the Architectury Loom Forge setup and explicitly declares only GTCEu as the mod dependency:

```groovy
modImplementation "com.gregtechceu.gtceu:gtceu-${project.minecraft_version}:${project.gtceu_version}"
```

It intentionally does not include the old AE2, Mekanism, KubeJS, GTMThings, ExtendedAE, GTMUtils, or Kotlin dependencies.

It also does not define a Gradle data-generation run or `src/generated/resources` source directory.

## Validation already attempted

### Initial failure

Before adding Fabric Maven, Gradle failed during configuration because Loom dependencies could not be found. Fabric Maven has now been added to both plugin management and project repositories.

### Java

The machine PATH has no `java`, and `JAVA_HOME` was initially unset. IntelliJ's bundled JBR was found and used:

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### `build`

Running `gradlew.bat build` with JBR 25 reaches Gradle configuration but fails before compilation while creating the default `test` task:

```text
Could not create task ':test'
Could not create an instance of type DefaultTestTaskReports
Type T not present
```

This appears to be Gradle/plugin/JBR compatibility or an unwanted default test task. The user needs `runClient`; do not let an empty `test` task block client development. Consider running `runClient` directly and/or disabling/removing the default test task if it is safe and appropriate.

### `compileJava`

Running `gradlew.bat compileJava` with JBR 25 reaches Java compilation but currently fails with these main problems:

1. `com.tterrag.registrate.providers.DataGenContext` is not on the compile classpath.
2. `com.tterrag.registrate` classes referenced by GTCEu are unavailable to javac (`AbstractRegistrate`, `RegistryEntry`, `NonNullBiConsumer`, etc.).
3. `com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern` does not exist in GTCEu v8. In v8 the pattern classes are under:

```text
com.gregtechceu.gtceu.api.multiblock.pattern
com.gregtechceu.gtceu.api.multiblock.util
```

4. `GTCEu` v8 uses `com.gregtechceu.gtceu.api.multiblock.Predicates`, not the old `api.pattern.Predicates`.
5. `GTMQoL.LOGGER` is referenced but missing.
6. Other errors involving `GTRegistrate.registerRegistrate()` and `GTBlockstateProvider.run()` may be secondary to missing Registrate types or may indicate API mismatches; verify against the actual v8 source/jars.

The local Gradle Loom cache contains:

```text
.gradle/loom-cache/remapped_mods/remapped/com/gregtechceu/gtceu/gtceu-1.20.1-95f0cc80/8.0.0-SNAPSHOT/
.gradle/loom-cache/remapped_mods/remapped/com/tterrag/registrate/Registrate-95f0cc80/MC1.20-1.3.11/
```

The local GTCEu v8 source jar confirms:

- `GTRegistrate` exists;
- `MachineBuilder` and `MultiblockMachineBuilder` exist;
- `RuntimeBlockstateProvider` exists in `com.gregtechceu.gtceu.utils.data`;
- `GTMachineModels.createBasicMachineModel(...)` and `createMachineModel(...)` exist;
- v8 uses `MultiblockPatternBuilder`, not `FactoryBlockPattern`.

## Immediate next-agent task

1. Use IntelliJ JBR or a compatible Java 17 runtime.
2. Make `compileJava` pass.
3. Make `runClient` configure and launch.
4. Do not add Kotlin.
5. Do not restore old features or old dependencies.
6. Keep integration examples as real registered content; do not return to hand-written JSON tests.
7. If the runtime generation approach is too coupled to data-generation-only classes, use GTCEu's public runtime builder/provider APIs and keep the implementation minimal.
8. After compilation, run:

```powershell
$env:JAVA_HOME='C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient --no-daemon
```

If the client is long-running, verify startup from the log and stop it with its specific process ID when necessary.

## Explicitly deferred work

Do not implement these yet:

- wireless Recipe Capability network;
- FE-to-EU;
- match groups / RecipeDB mixins;
- AE2 integration;
- overclocking or parallelism patches;
- generic GTDynamicDataPack writer;
- broad machine migration from `reference/`.

First prove that a clean Java project can start the client and dynamically register/display one single-block machine and one multiblock machine.
