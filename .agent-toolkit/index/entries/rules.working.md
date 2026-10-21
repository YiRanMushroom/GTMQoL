+++
title = "How to work in GTMQoL 1.20.1: Chinese replies, run don't guess, Gradle limits, port from 1.21, warnings"
keys = [ "author", "branches", "chinese", "gradle", "rules", "warnings" ]
updated = "2026-10-21 08:00:54"
+++
Personal, non-serious project, not a production system.

- Reply to the user in Chinese.
- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: you may run build / compileJava / runData. Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, JEI/EMI entries).
- GTCEu sources for this branch: this worktree's `.gtceu-src` (1.21's are `.gtceu-src-1.21` in its worktree); if unsure, check which sources jar a directory matches before quoting it.
- Features are written on 1.21 first and ported here (open items: CLAUDE.md's context.md files). This index has the same ids as 1.21's, linked to this branch's code.
- README.md is English only (credits in its Credits section). Author: README/LICENSE "Yiran"; in-game mod_author "Yiran, Frosty" (Frosty is the user's alias).
- UI text: short, plain GT-style tooltips saying what something does, only what affects the player's decision (no tick counts, batch sizes, nothing the structure preview already shows); lang key: rules.machine-tooltip-lang. UI windows centered and roomy, not small side panels.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; the only accepted warnings are Forge's BuiltInRegistries deprecation (kept so code matches 1.21).