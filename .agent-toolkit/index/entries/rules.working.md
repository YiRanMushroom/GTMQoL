+++
title = "How to work in GTMQoL 1.20.1: Chinese replies, run don't guess, Gradle limits, port from 1.21, warnings"
keys = [ "author" ]
updated = "2026-10-21 01:15:48"
+++
Personal, non-serious project, not a production system.

- Reply to the user in Chinese.
- Finding things: same ids and key tree as on 1.21, linked to this branch's code. index_keys("gtmqol") lists the areas (one per id prefix); index_find("<area>") lists an area's entries and, for the big ones, its topics (subkeys), which index_find narrows to. Build setup: build.*.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData, with JAVA_HOME set to JDK 21 (the Gradle JVM; Java 17 is only the toolchain, build.environment). Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, JEI/EMI entries).
- GTCEu sources for this branch: this worktree's `.gtceu-src` (1.21's are `.gtceu-src-1.21` in its worktree); if unsure, check which sources jar a directory matches before quoting it.
- Features are written on 1.21 first and ported here (open items: CLAUDE.md's context.md files).
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- UI text: short, plain GT-style tooltips saying what something does, only what affects the player's decision (no tick counts, batch sizes, nothing the structure preview already shows); lang key: rules.machine-tooltip-lang. UI windows centered and roomy, not small side panels.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; the only accepted warnings are Forge's BuiltInRegistries deprecation (kept so code matches 1.21).