+++
title = "Project rules: run instead of guessing, what Gradle tasks may run, both branches, README, UI text, lint"
keys = [ "conventions" ]
updated = "2026-10-21 05:50:26"
+++
Personal, non-serious project, not a production system. The general rules come with agent-toolkit's server instructions (the same in every project); these are this project's own.

- Run, don't guess: don't grep/read widely to guess where a problem is. Get a build or a run, take the real error, then read that one spot. Compiler and logs are the reliable sources; inferred APIs need a build to verify. One error at a time, no speculative dependencies or code.
- Gradle: Claude may run build / compileJava / runData. Never runClient / runServer or anything that starts the game; no `tasks` / `dependencies` exploration. For in-game checks tell the user exactly what to look for (log keywords, items/blocks, EMI/JEI entries).
- Features go on both branches (see context.md).
- README.md is English only (credits in its Credits section).
- UI text: short GT-style tooltips.
- javac runs with -Xlint:deprecation,removal,unchecked,rawtypes; keep the build warning-free (1.20.1 only has Forge's BuiltInRegistries deprecation, kept so code matches 1.21).