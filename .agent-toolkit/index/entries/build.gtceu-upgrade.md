+++
title = "How to bump the pinned GTCEu v8 snapshot (and ModularUI with it) on 1.20.1, and what to recheck"
keys = [ "1.21", "gtceu_min_version", "gtceu_version", "mixin targets", "modularui_version", "mui", "require", "sources jar", "version pin" ]
updated = "2026-11-07 10:17:44"
checked = "2026-11-07"

[[items]]
name = "pinned-version"
value = "The pinned GTCEu snapshot coordinate (bumped by hand)."
path = "gradle.properties"
snippet = "gtceu_version="

[[items]]
name = "mui-version"
value = "The pinned MUI snapshot, bumped with GTCEu."
path = "gradle.properties"
snippet = "modularui_version="

[[items]]
name = "delegating-ctor"
value = "Example of a constructor that delegates via this(...) since build 327: only the full descriptor is targeted."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/SimpleSteamMachineMixin.java"
snippet = "// so \"<init>*\" would attach everything twice."

[[items]]
name = "require-count"
value = "Example require count to recheck (steam output limits)."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMultiMachinesMixin.java"
snippet = "require = 2)"

[[items]]
name = "manual-compression"
value = "Method list to compare with GTCEu's uses of disableManualCompression."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/ManualCompressionMixin.java"
snippet = "@ModifyExpressionValue(method = { \"disableManualCompression\", \"init\", \"processNugget\", \"processBlock\","
+++
gradle.properties pins `gtceu_version` to a timestamped snapshot (1.20.1 artifact) and `modularui_version`; both are bumped by hand. 1.21.1 does the same with its own artifact.

Steps:
1. Pick the newest build from maven.gtceu.com `.../8.0.0-SNAPSHOT/maven-metadata.xml`.
2. Check upstream `gradle/forge.versions.toml` / `libs.versions.toml` on GTCEu's `1.20.1` branch (registrate, configuration, AE2, EMI, JEI, `mui`). Even with `mui = 3.3.1-SNAPSHOT` unchanged the MUI snapshot moves: take the MUI build current when the GTCEu snapshot was published, or MUI mismatches show up as `NoSuchMethodError` at runtime.
3. Diff the old and new sources jars (unpacked in `.gtceu-src`; MUI sources in `.mui-src`; re-extract both on every bump, a stale copy hides changes: the build number of a snapshot doesn't say which commits it holds, so diff the jars rather than the GitHub commit range) and reread every mixin target, including constructors that start delegating with this(...) (`<init>*` then runs twice, SimpleSteamMachineMixin at 327): injection points, `require =` counts (grep `require =`; e.g. `GTMachinesMixin` 1, `GTMultiMachinesMixin` 2 for the steam output limits, `GTMachineUtilsMixin` 2 for the large boiler, `MEPatternBufferPartMachineMixin` 5; a wrong count crashes at load), regex lambda selectors (`GTMultiMachinesMixin`, `GTMachineUtilsMixin`, `GTSingleblockMachinePanelsMixin`, `GTMachinesMixin`) and fixed javac lambda names (`ResearchOverclockMixin` `lambda$build$1`; RecipeDB's in step 7).
4. For `gtceufix/` mixins check whether upstream fixed the bug and delete them then (gtceufix.overview). The MUI targets (`AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`) need a check against the new MUI snapshot too.
5. `ManualCompressionMixin`: grep GTCEu for `disableManualCompression` and compare with its method list (steam.manual-compression). Same for `removeVanillaTNTRecipe` / `hardGlassRecipes` in `RecipeRemovalMixin`.
6. Check APIs we rely on (2026-10 examples: `ChemicalHelper.getItem` returns null instead of AIR, `MultiPredicate.getParent` removed by the predicate rework #5342/#5407).
7. RecipeDB grouped search: `RecipeDB.fromHolder`, `getNext`, the lambda name, `SearchFrame.index`, the buffer's `workers` / `Worker.slot` (accessor mixins), and the `RecipeRunner` grouping rules `canFollow` mirrors (recipedb.v8-handler-groups).
8. Look for new upstream features overlapping ours.

`gtceu_min_version` (mods.toml lower bound, `8.0.0-SNAPSHOT`) stays separate from the pinned coordinate because GTCEu reports `8.0.0-SNAPSHOT+<commit>` at runtime.