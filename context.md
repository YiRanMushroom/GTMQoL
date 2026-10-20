# GTMQoL Current Context (1.20.1)

Handoff for a new session: what is specific to this branch and open right now. Rules, build setup and design are in
the doc index (see `CLAUDE.md`), not here.

## Branches

Features are written on `port/1.21.1` (= `master`) first and ported here. Its `context.md` holds the shared to-do
list and open questions; read it too. The 1.21.1 worktree sits next to this one (`..\GTMQoL`); the user works on two
computers, so paths differ (`C:\Users\yiran.wang\IdeaProjects\` or `E:\code\Minecraft\`): check
`git worktree list`, or `git show port/1.21.1:context.md`.

- This branch: `migrate/gtceu-v8-runtime-generation`. Minecraft 1.20.1, Forge 47, Java 17, ModDevGradle legacyForge.
  GTCEu v8 snapshot pinned in `gradle.properties`. Java only. Not run in game yet.
- Not on this branch: MI gregification (no Forge 1.20.1 build of MI); late regeneration for KubeJS machines.
  Code differences: `build.branch-differences`.

## Doc index

Same ids and key tree as on 1.21.1, linked to this branch's code: `index_keys("gtmqol")` lists the areas,
`index_find("<area>")` its entries and topics.

## To do

- First client run under ModDevGradle: the checks in `build.first-run-checks`.

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (seen on 1.21.1; this branch's ModularUI
  `3.3.1-20261013` has the same `ThemeManager` code, so most likely here too). Details and the possible fix are in
  1.21.1's `context.md`.
- Proxy pattern buffers don't get the pattern output marks (as on 1.21.1).
- Upstream: steam (BRONZE theme) panel title and GT logo squeezed, GTCEu's own steam machines too.

## Recent

- 2026-10-20: doc index reorganized like 1.21.1's (key tree, fewer keys, stale and duplicate entries removed);
  `reference/` deleted; mixin config plugins share a base class; splash / lingering potion fluids ported; all popups share `PopupWidgets`. Ported
  earlier the same day: the potion "any mod's version" hint, shared hatches gating fix, block provider lookup fix,
  greenhouse batch mode.
