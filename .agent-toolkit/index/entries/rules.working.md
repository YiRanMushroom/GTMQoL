+++
title = "How to work in GTMQoL: reply in Chinese, run don't guess, Gradle limits, both branches, README, UI text, warnings"
keys = [ "author", "branches", "chinese", "gradle", "rules", "warnings" ]
updated = "2026-10-21 08:00:47"
+++
Personal, non-serious project, not a production system.

- Reply to the user in Chinese.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData. Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, EMI/JEI entries).
- GTCEu sources for this branch: `.gtceu-src-1.21`, never `.gtceu-src` (an older extraction); if unsure, check which sources jar a directory matches before quoting it.
- Features go on both branches, 1.21 and 1.20 (worktrees and paths: context.md; code differences: build.branch-differences).
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- UI text: short, plain GT-style tooltips saying what something does, only what affects the player's decision (no tick counts, batch sizes, nothing the structure preview already shows); lang key: rules.machine-tooltip-lang. UI windows centered and roomy, not small side panels.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; keep the build warning-free (1.20.1 only has Forge's BuiltInRegistries deprecation, kept so code matches 1.21).