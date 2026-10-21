# GTMQoL Current Context

Handoff for a new session: general working notes and what is open right now. Rules, build setup and design are in
the doc index (see `CLAUDE.md`), not here.

## Branches

Two long-lived branches, kept in sync with cherry-picks. Features go on both unless marked 1.21 only.

| Branch | Version | Notes |
|---|---|---|
| `1.21` (this checkout, the default branch) | NeoForge 21.1, Java 21 | Features are written here first |
| `1.20` | Forge 47, Java 17 | ModDevGradle legacyForge |

- The 1.20.1 worktree sits next to this one (`..\GTMQoL-1.20.1`). The user works on two computers, so paths differ
  (`C:\Users\yiran.wang\IdeaProjects\` or `E:\code\Minecraft\`): check `git worktree list` and the checked-out branch
  before porting.
- 1.21 only: MI gregification (MI has no Forge 1.20.1 build); KubeJS machines' late regeneration (modular and
  electric implosion copies of KubeJS recipes). Other differences: `build.branch-differences`.
- GTCEu v8 snapshot, pinned on both (`gradle.properties`, bumps: `build.gtceu-upgrade`). Java only.

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (user: leave it; workaround is to not reload, restart
  if hit). `ClassCastException: IDrawable$2 (NONE) cannot be cast to UITexture` in `MachineUIPanel.<init>`. Cause:
  ModularUI 3.3.1-SNAPSHOT `ThemeManager` merges the java theme with a shallow `addAllOf`, then `parse` writes
  `background: "none"` into GTCEu's registered `GTGuiTheme` JSON (still so in MUI `3.3.1-20261013`). Possible fix:
  a `gtceufix/` client mixin passing `builder.getJson().deepCopy()`.
- Proxy pattern buffers don't get the pattern output marks, neither in the recipe lookup nor in grouped search
  (`ae2.pattern-output-marks`, `PatternBufferIngredients`).
- Upstream: on steam (BRONZE theme) panels the title and GT logo are pushed right and squeezed, GTCEu's own steam
  machines too.

## To do

- The unpacked GTCEu sources are older than the pinned snapshots: `.gtceu-src-1.21` is build 20261001 (pinned
  20261014.192230-122), 1.20's `.gtceu-src` 20260926 (pinned 20261013.044024-319). Re-unpack from the Gradle cache
  (ask the user first, the folders are theirs).
- Auto-build: substructures, then block choices for expandable patterns (`autobuild.*`).
- Stack-like: external chemical hatches.

## Open questions for the user

- Two stocking parts of one multiblock can't stock the same key (gtceu's rule, extended to ours by `StockingParts`).
  The user dislikes it but has no better idea: both would see the full network amount and the recipe check would
  count it twice.
- GTCEu now makes the ME buses/hatches, stocking parts, pattern buffer and proxy in the assembler (#5518/#5519); our
  `AEProcessing` ME assembler recipes for the same parts are a second path. Keep or drop?
