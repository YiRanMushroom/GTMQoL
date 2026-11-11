+++
title = "Direction: ship good features fast and keep iterating; light process, but a public mod players install"
keys = [ "definition of done", "direction", "priorities", "quality bar", "when to ask" ]
updated = "2026-11-11 08:19:21"
checked = "2026-11-11"

[[items]]
name = "optional-tags"
value = "The helpers for tagging content that may not be registered"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoLAddon.java"
snippet = "public static void optionalItemTag(TagKey<Item> tag, String item) {"
+++
GTMQoL is a public GTCEu addon that one person builds for fun and releases for others to play. The aim: turn ideas into good features quickly and keep iterating. It is not a production system, so no formal process: no sign-off steps, no documents or tests for their own sake. But players install every release, so what ships has to be right.

## Fast
- Decide and act. Design, names, layout and small refactors inside these rules are yours to choose; say what you chose when you report. A wrong choice costs one more round, and so does a question.
- Ask first only for what is hard to take back or is a matter of the user's taste: what players get (balance numbers, defaults, removing something), a change that breaks saved worlds or configs, a new abstraction or a hack (general.design), committing and releasing (general.commits).
- A request is finished in one go: the code on both branches, its lang (runData), config comment, README, index entry and Untested notes. Nothing that belongs to the change is left for later.
- Small and direct beats general: write what the feature needs now. Shared code comes when the same logic exists a third time, not before.
- Long work (builds, runData, reviews by subagents) runs in the background while you go on (general.long-tasks).

## Good
- The bar: a player with any supported setup updates and nothing breaks. Every optional mod missing or installed, every config toggle either way, an old world, an old config, a dedicated server.
  - Old worlds: block, item and machine ids and saved field names stay; a rename goes through RenamedIds (skill.naming-and-text).
  - Old configs: a renamed key silently goes back to its default. Rename one only when its name is wrong, and say so in the release notes.
  - Optional content (behind a config toggle or an optional mod) is never a required reference in generated data: tags take it through GTMQoLAddon.optionalItemTag / optionalBlockTag, since one missing required entry unloads the whole tag for every mod. Its classes are only touched behind the mod check.
  - Static resources (datagen: tags, lang, models, recipes) are for our own content and for what concerns GTCEu and AE2. What concerns any other mod (Mekanism, MI, Create, HNN, Productive Bees, ...) is added at runtime and only with that mod loaded: tags through the tag map hook in MixinHelpersMixin (WireTags, CircuitTags), lang and models through RuntimeGeneration, recipes in addRecipes behind the mod check. The user's rule (2026-11-11); the Mekanism circuit tags were the one static case and moved to runtime then.
  - Amounts that can grow without bound (EU, items, fluids, parallels) are longs or capped; hitting an int limit is this project's most common bug.
- Check what can be checked without the game before calling it done: both branches compile with no new warnings, runData runs and its diff is what was meant (it also runs MixinTargetCheck), unit tests for pure logic, the IDE's inspections on the files you touched (rules.working).
- What needs the game is the user's to test: an Untested: comment plus a status.untested item that says exactly what to do and what to see (general.knowledge). Never report it as working.
- Text players read is part of the feature (skill.naming-and-text): a wrong tooltip or README line is a bug like any other.
- Leave the code you touch a little cleaner (dead code, a stale comment, a missing @Nullable), without growing the change into a refactor nobody asked for.

## Releases
skill.release. The user says when and which version: a patch for fixes and small changes, a minor for a feature players would notice.