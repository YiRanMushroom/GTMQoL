# GTMQoL Current Context (1.20.1)

Handoff for a new session: what is specific to this branch and open right now. Rules, build setup and design are in
the doc index (see `CLAUDE.md`), not here.

## Branches

Features are written on `1.21` first and ported here. Its `context.md` holds the shared to-do
list and open questions; read it too. The 1.21.1 worktree sits next to this one (`..\GTMQoL`); the user works on two
computers, so paths differ (`C:\Users\yiran.wang\IdeaProjects\` or `E:\code\Minecraft\`): check
`git worktree list`, or `git show 1.21:context.md`.

- This branch: `1.20`. Minecraft 1.20.1, Forge 47, Java 17, ModDevGradle legacyForge.
  GTCEu v8 snapshot pinned in `gradle.properties`. Java only.
- Not on this branch: MI gregification (no Forge 1.20.1 build of MI); late regeneration for KubeJS machines.
  Code differences: `build.branch-differences`.

## To do

- Untested changes to remind the user of (first client run, in-game checks): `status.untested`, each item linked to
  its `Untested:` comment in the code.

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (seen on 1.21.1; this branch's ModularUI
  `3.3.1-20261026` has the same `ThemeManager` code as `-20261013`, so most likely here too). Details and the possible fix are in
  1.21.1's `context.md` (ModularUI `3.3.1-20261022` not checked).
- Proxy pattern buffers don't get the pattern output marks (as on 1.21.1).
- Upstream: steam (BRONZE theme) panel title and GT logo squeezed, GTCEu's own steam machines too.
