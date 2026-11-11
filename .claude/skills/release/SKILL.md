---
name: release
description: "Release: version bump, both branches built and pushed, GitHub releases with notes; use when the user asks to release"
---

<!-- agent-toolkit: generated from index entry skill.release: change it with index_update("skill.release"), not here -->

Use when the user asks for a release. They name the version (patch: fixes and small changes; minor: a feature players would notice).

## Before
- Everything meant for the release is on both branches (worktrees: context.md), README included.
- runData is clean on both, one branch at a time, and the generated files are committed with the change.

## Steps
1. `mod_version` in gradle.properties of both worktrees.
2. Commit on each branch (general.commits: no attribution): one line "<version>: <what changed>", in the style of git log; the 1.20 message names what differs there.
3. Build each with JAVA_HOME on JDK 21, one after the other, in the background: `./gradlew build --console=plain -q`. Never clean (rules.working). Jars: build/libs/gtmqol-<version>-1.21.1.jar and gtmqol-<version>-1.20.1.jar (not the -sources ones).
4. Push both branches (1.21, 1.20).
5. Two GitHub releases, 1.20 first so 1.21 ends up as Latest:
   - `gh release create v<version>-1.20 <1.20 jar> --target 1.20 --title "<version> (1.20)" --notes-file <file> --latest=false`
   - `gh release create v<version>-1.21 <1.21 jar> --target 1.21 --title "<version> (1.21)" --notes-file <file> --latest`
6. Check: `gh release list -L 2`, and both worktrees are level with origin.

## Notes
English, written for players (skill.naming-and-text): what is different in the game, not how it was built. Changes only developers see (build setup, refactors) are left out.
- First line, 1.21: "Minecraft 1.21.1, NeoForge 21.1. Built against GTCEu Modern `<gtceu_version>` (v8 snapshot). Use this GTCEu build: older ones don't work with this release." 1.20: "Minecraft 1.20.1, Forge 47. ..." with that branch's gtceu_version (each from its gradle.properties).
- Then "### New", "### Changed", "### Fixed", only the ones that have entries; feature names in bold; one bullet per thing a player would notice.
- Anything the player has to do or may lose (a renamed config key, a changed recipe, a block that moved) is said here.
- 1.21-only or 1.20-only changes appear only in that branch's notes.
- Last line: "Full feature list and config: see the README."

## After
Tell the user which changes in the release nobody has tried in game yet (status.untested), each with what to look for.

- version: The version both branches bump

Code:
- gradle.properties
