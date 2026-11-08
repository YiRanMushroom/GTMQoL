+++
title = "Our pattern batching and crafting planner: batches cut from the plan, topological plans, no EAP, unit tested"
kind = "design"
keys = [ "ae2", "assembling matrix", "crafting plan", "pattern batch", "pattern batching", "topological planner" ]
updated = "2026-11-08 03:57:57"
checked = "2026-11-08"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/CraftingSolver.java"
snippet = "public final class CraftingSolver"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/batch/BatchPlan.java"
snippet = "public final class BatchPlan"
+++
Written on 1.21, same design here: read this entry there (repo GTMQoL, branch 1.21). 1.20 differences: build.branch-differences. In short: pieces cut from the plan, a hidden item so jobs survive restarts, a plain Java planner with unit tests, AE2's logic for what it refuses, and AE2's own CPU pushing part of a batch it can't push whole (PartialPush, unit tested). Mixins can be checked without the game: Class.forName(name, false, loader) in init, then runData.