+++
title = "How to work in GTMQoL: reply in Chinese, run don't guess, Gradle limits, both branches, README, UI text, warnings"
keys = [ "author" ]
updated = "2026-11-08 04:16:55"
checked = "2026-11-08"
+++
Personal, non-serious project, not a production system.

- Reply to the user in Chinese.
- Finding things: index_keys("gtmqol") lists the areas (one per id prefix); index_find("<area>") lists an area's entries and, for the big ones, its topics (subkeys), which index_find narrows to. Build setup: build.*.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData, with JAVA_HOME set to JDK 21 (build.environment; the default java may be older). Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, EMI/JEI entries).
- Never rerun-tasks or clean while the user may be in game: the dev client loads from build/classes, so emptying it crashes the running game with NoClassDefFoundError on classes not loaded yet. Plain compileJava only changes what changed.
- Unit tests: gradlew test, plain JUnit, no game; only classes without game classes (integration/ae2/batch). Fast, so keep adding to them.
- GTCEu sources for this branch: `.gtceu-src-1.21`, never `.gtceu-src` (an older extraction); if unsure, check which sources jar a directory matches before quoting it.
- Features go on both branches, 1.21 and 1.20 (worktrees and paths: context.md; code differences: build.branch-differences).
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- Names, IDs and text players see: skill.naming-and-text (read it whenever naming something). UIs: skill.modular-ui. Mixins: skill.mixins.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; keep the build warning-free (1.20.1 only has Forge's BuiltInRegistries deprecation, kept so code matches 1.21).