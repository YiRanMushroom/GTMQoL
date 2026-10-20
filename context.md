# GTMQoL Current Context (1.20.1)

Handoff for a new session: what is specific to this branch and open right now. Build environment and rules are in
`CLAUDE.md` (read it first). Design and how things work are in the doc index, not here.

## Branches

Features are written on `port/1.21.1` (= `master`) first and ported here. Its `context.md` holds the shared to-do
list, open questions and conventions; read it too. The 1.21.1 worktree sits next to this one (`..\GTMQoL`); the user
works on two computers, so paths differ (`C:\Users\yiran.wang\IdeaProjects\` or `E:\code\Minecraft\`): check
`git worktree list`, or `git show port/1.21.1:context.md`.

- This branch: `migrate/gtceu-v8-runtime-generation`. Minecraft 1.20.1, Forge 47, Java 17, ModDevGradle legacyForge
  (moved from Loom in `3b63374`). GTCEu v8 snapshot pinned in `gradle.properties`. Java only.
- Not on this branch: MI gregification (no Forge 1.20.1 build of MI); late regeneration for KubeJS machines.
  Code differences: `port.differences-1.20.1`.
- `reference/` is the archived old implementation: research only, don't migrate it wholesale or modify it.

## Doc index

`index_open("<repo root>")`, then `index_find`. Never read `.agent-index.db` directly. Same ids as on 1.21.1,
linked to this branch's code. Prefixes are listed in 1.21.1's `context.md`; `layout.source` has the package tree.

## To do

- First client run under ModDevGradle: the checks in `build.first-run-checks` (Jade `this$0`, EvalEx, ProductiveLib,
  gtceu jarjar, run config).

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (seen on 1.21.1; not checked whether 1.20.1's
  ModularUI build has the same code). Details and the possible fix are in 1.21.1's `context.md`.
- Upstream: steam (BRONZE theme) panel title and GT logo squeezed, GTCEu's own steam machines too.

## Recent

- 2026-10-20: context trimmed, its design notes moved into the doc index. Ported from 1.21: the potion "any mod's
  version" hint, shared hatches gating fix, block provider lookup fix, greenhouse batch mode.
