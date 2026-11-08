+++
title = "GTCEu upgrade: bump the pinned GTCEu v8 snapshot (and ModularUI) and what to recheck; use when upgrading GTCEu"
keys = [ "1.20.1", "gtceu_version", "mixin targets", "modularui_version", "mui", "require", "sources jar", "version pin" ]
updated = "2026-11-08 19:27:13"
checked = "2026-11-08"

[[items]]
name = "pinned-versions"
value = "gtceu_version / modularui_version pinned snapshots (step 1-2); MUI must match the build GTCEu was compiled against."
path = "gradle.properties"
snippet = "# Must match the mui version gtceu was built against"

[[items]]
name = "min-version"
value = "gtceu_min_version is the mods.toml lower bound, separate from the pinned coordinate."
path = "src/main/resources/META-INF/neoforge.mods.toml"
snippet = "versionRange = \"[${gtceu_min_version},)\""

[[items]]
name = "require-count"
value = "Step 3 example: a require count that crashes at load when GTCEu changes its calls (the large boiler's two setExactLimit calls)."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMachineUtilsMixin.java"
snippet = "require = 2)"

[[items]]
name = "recipedb-hooks"
value = "Step 7: RecipeDB.fromHolder replaced by GroupedIngredientList (recipedb.v8-handler-groups)."
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/recipedb/RecipeDBMixin.java"
snippet = "GroupedIngredientList list = GroupedIngredientList.fromHolder(holder);"

[[items]]
name = "buffer-at"
value = "Step 7: AT on the pattern buffer's workers / Worker.slot."
path = "src/main/resources/META-INF/accesstransformer.cfg"
snippet = "public com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$Worker slot"
+++
Both branches pin GTCEu to a timestamped snapshot (`gtceu_version` in gradle.properties) and bump it by hand.

Steps:
1. Pick the newest build from maven.gtceu.com `.../8.0.0-SNAPSHOT/maven-metadata.xml` (1.21 artifact `gtceu-1.21.1`).
2. Check upstream `gradle/forge.versions.toml` / `libs.versions.toml` (registrate, configuration, AE2, EMI, JEI, NeoForge, `mui`). Even when `mui = 3.3.1-SNAPSHOT` is unchanged, the MUI snapshot itself moves: bump `modularui_version` to the MUI build current when the GTCEu snapshot was published, or MUI API mismatches show up as `NoSuchMethodError` at runtime.
3. Re-unpack the new GTCEu sources jar (Gradle cache) into `.gtceu-src-1.21` / the 1.20 worktree's `.gtceu-src`, so those folders match the pinned version, diff against the old one, and read every mixin target against the new code: injection points, `require =` counts (grep `require =`, e.g. `GTMachineUtilsMixin` 2, `MEPatternBufferPartMachineMixin` 5; a wrong count crashes at load), lambda synthetic names (`GTMultiMachinesMixin`, `GTMachineUtilsMixin` use regex selectors), and `<init>*` injections when a constructor starts delegating with `this(...)` (they would run twice, as 1.20 build 327 did to `SimpleSteamMachine`, #5597). Features on existing machines go through MachineFeatures (api.machine-features), which runs after the whole constructor and isn't affected.
4. For `gtceufix/` mixins check whether upstream fixed the bug (delete then, see gtceufix.overview). ModularUI/JEI targets (`AbstractWidgetMenuMixin`, `BaseSchemaRendererMixin`, `jei/JeiRecipeSlotMixin`, `ItemDataComponentMapIngredientMixin`) need a check against the new MUI snapshot too.
5. `ManualCompressionMixin`: grep GTCEu for `disableManualCompression` and compare the target list (see steam.manual-compression).
6. Check API changes we rely on (example from 2026-10: `ChemicalHelper.getItem` returns null instead of AIR, `MultiPredicate.getParent` removed by the predicate rework #5342/#5407; 1.21 #5414 made `GTMaterials.X` / `TagPrefix.x` registry entries: add `.value()` where a plain object is needed, and nothing may resolve them during mod construction (`Registry not present for DeferredHolder`, only seen at runData: defer to `PostMaterialEvent` or a supplier); 1.20 #5337 reworked the structure helpers, see autobuild.planner).
7. RecipeDB grouped search: `RecipeDB.fromHolder`, `getNext`, the lambda name, `SearchFrame.index`, the buffer's `workers` / `Worker.slot` (AT), and the `RecipeRunner` grouping rules `canFollow` mirrors (recipedb.v8-handler-groups).
8. Look for new upstream features overlapping ours.

`gtceu_min_version` (mods.toml lower bound) stays separate from the pinned coordinate: GTCEu reports `8.0.0-SNAPSHOT+<commit>` at runtime.
