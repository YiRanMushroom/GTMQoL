+++
title = "How to bump the pinned GTCEu v8 snapshot (and ModularUI with it) on 1.20.1, and what to recheck"
keys = [ "1.21", "gtceu_min_version", "gtceu_version", "mixin targets", "modularui_version", "mui", "require", "sources jar", "version pin" ]
updated = "2026-10-29 05:08:25"
checked = "2026-10-29"
+++
gradle.properties pins `gtceu_version` to a timestamped snapshot (1.20.1 artifact) and `modularui_version`; both are bumped by hand. 1.21.1 does the same with its own artifact.

Steps:
1. Pick the newest build from maven.gtceu.com `.../8.0.0-SNAPSHOT/maven-metadata.xml`.
2. Check upstream `gradle/forge.versions.toml` / `libs.versions.toml` on GTCEu's `1.20.1` branch (registrate, configuration, AE2, EMI, JEI, `mui`). Even with `mui = 3.3.1-SNAPSHOT` unchanged the MUI snapshot moves: take the MUI build current when the GTCEu snapshot was published, or MUI mismatches show up as `NoSuchMethodError` at runtime.
3. Diff the old and new sources jars (unpacked in `.gtceu-src`; MUI sources in `.mui-src`; re-extract both on every bump, a stale copy hides changes: the build number of a snapshot doesn't say which commits it holds, so diff the jars rather than the GitHub commit range) and reread every mixin target, including constructors that start delegating with this(...) (`<init>*` then runs twice, SimpleSteamMachineMixin at 327): injection points, `require =` counts (`GTMachinesMixin` 1, `GTMultiMachinesMixin` 2 for the steam output limits, `GTMachineUtilsMixin` 2 for the large boiler; a wrong count crashes at load), regex lambda selectors (`GTMultiMachinesMixin`, `GTMachineUtilsMixin`, `GTSingleblockMachinePanelsMixin`, `GTMachinesMixin`).
4. For `gtceufix/` mixins check whether upstream fixed the bug and delete them then (gtceufix.overview). The MUI targets (`AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `DummyChunkSourceMixin`) need a check against the new MUI snapshot too.
5. `ManualCompressionMixin`: grep GTCEu for `disableManualCompression` and compare with its method list (steam.manual-compression). Same for `removeVanillaTNTRecipe` / `hardGlassRecipes` in `RecipeRemovalMixin`.
6. Check APIs we rely on (2026-10 examples: `ChemicalHelper.getItem` returns null instead of AIR, `MultiPredicate.getParent` removed by the predicate rework #5342/#5407).
7. RecipeDB grouped search: `RecipeDB.fromHolder`, `getNext`, the lambda name, `SearchFrame.index`, the buffer's `workers` / `Worker.slot` (accessor mixins), and the `RecipeRunner` grouping rules `canFollow` mirrors (recipedb.v8-handler-groups).
8. Look for new upstream features overlapping ours.

`gtceu_min_version` (mods.toml lower bound, `8.0.0-SNAPSHOT`) stays separate from the pinned coordinate because GTCEu reports `8.0.0-SNAPSHOT+<commit>` at runtime.