+++
title = "How to work in GTMQoL: reply in Chinese, run don't guess, Gradle limits, both branches, README, UI text, warnings"
keys = [ "author" ]
updated = "2026-11-11 08:03:06"
checked = "2026-11-11"
+++
What the project is for and what counts as done: rules.direction. This entry is the mechanics.

- Reply to the user in Chinese.
- Finding things: index_keys("gtmqol") lists the areas (one per id prefix); index_find("<area>") lists an area's entries and, for the big ones, its topics (subkeys), which index_find narrows to. Build setup: build.*.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData / test, through `bash .agent-toolkit/gradle.sh <21|20|both> <tasks...>` (sets JAVA_HOME to JDK 21, runs the branches one after the other, prints only errors, new warnings, the mixin target count and the exit codes; build.environment). verify() is `both compileJava`; data runs and tests are a job: sh_exec("bash .agent-toolkit/gradle.sh both runData test"). Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, EMI/JEI entries).
- Never rerun-tasks or clean while the user may be in game: the dev client loads from build/classes, so emptying it crashes the running game with NoClassDefFoundError on classes not loaded yet. Plain compileJava only changes what changed.
- Unit tests: gradlew test, plain JUnit, no game; only classes without game classes (integration/ae2/crafting). Fast, so keep adding to them.
- GTCEu sources for this branch: `.gtceu-src-1.21`, never `.gtceu-src` (an older extraction); if unsure, check which sources jar a directory matches before quoting it.
- Features go on both branches, 1.21 and 1.20 (worktrees and paths: context.md; code differences: build.branch-differences). ts_exec has helpers for changing both at once, loaded in every session from .agent-toolkit/ts/startup.ts: both(rel, [[old, new]]) (exact edits, a mismatch on one branch is reported and skipped), lang(rel, oldText, newText) (a lang string, also one split over lines), show(rel, from, to, branch), addImport(branch, rel, line), cleanImports(). runData is the check that finds what compileJava doesn't (lang duplicates, a class loaded too early on 1.20: build.early-class-load there).
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- Names, IDs and text players see: skill.naming-and-text (read it whenever naming something). UIs: skill.modular-ui. Mixins: skill.mixins.
- Case conversion and number formatting take Locale.ROOT (toLowerCase(Locale.ROOT), String.format(Locale.ROOT, ...)): a Turkish system turns I into a dotless i in keys, a German one writes 1,50.
- Nullability: every package has a package-info.java with @ParametersAreNonnullByDefault and @MethodsReturnNonnullByDefault (as GTCEu's packages; a new package gets one too), so only what can be null is marked, with org.jetbrains @Nullable; no @NotNull. Not fields (static registry entries are set in init()) and not the mixin packages (core/mixins: a non-mixin class there breaks loading).
- IntelliJ's inspections (ide_diagnostics on a file) find null risks, unused code and unused imports; its "redundant suppression" on `new Foo[n]` of a generic type is wrong for javac, which still warns rawtypes.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; keep the build warning-free (1.20.1 only has Forge's BuiltInRegistries deprecation, kept so code matches 1.21).