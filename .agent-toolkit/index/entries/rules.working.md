+++
title = "How to work in GTMQoL 1.20.1: Chinese replies, run don't guess, Gradle limits, port from 1.21, warnings"
keys = [ "author" ]
updated = "2026-11-11 07:17:17"
checked = "2026-11-11"
+++
What the project is for and what counts as done: rules.direction. This entry is the mechanics.

- Reply to the user in Chinese.
- Finding things: same ids and key tree as on 1.21, linked to this branch's code. index_keys("gtmqol") lists the areas (one per id prefix); index_find("<area>") lists an area's entries and, for the big ones, its topics (subkeys), which index_find narrows to. Build setup: build.*.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData, with JAVA_HOME set to JDK 21 (the Gradle JVM; Java 17 is only the toolchain, build.environment). Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, JEI/EMI entries).
- GTCEu sources for this branch: this worktree's `.gtceu-src` (1.21's are `.gtceu-src-1.21` in its worktree); if unsure, check which sources jar a directory matches before quoting it.
- Features are written on 1.21 first and ported here (open items: CLAUDE.md's context.md files).
- Never rerun-tasks or clean while the user may be in game: the dev client loads from build/classes, so emptying it crashes the running game with NoClassDefFoundError on classes not loaded yet. Plain compileJava only changes what changed.
- Unit tests: gradlew test, plain JUnit, no game; only classes without game classes (integration/ae2/crafting). Fast, so keep adding to them.
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- Names, IDs and text players see: skill.naming-and-text (read it whenever naming something). UIs: skill.modular-ui. Mixins: skill.mixins.
- Case conversion and number formatting take Locale.ROOT (toLowerCase(Locale.ROOT), String.format(Locale.ROOT, ...)): a Turkish system turns I into a dotless i in keys, a German one writes 1,50.
- Nullability: every package has a package-info.java with @ParametersAreNonnullByDefault and @MethodsReturnNonnullByDefault (as GTCEu's packages; a new package gets one too), so only what can be null is marked, with org.jetbrains @Nullable; no @NotNull. Not fields (static registry entries are set in init()) and not the mixin packages (core/mixins: a non-mixin class there breaks loading).
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; the only accepted warnings are Forge's BuiltInRegistries deprecation (kept so code matches 1.21).