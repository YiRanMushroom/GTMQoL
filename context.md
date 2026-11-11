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
- GTCEu v8 snapshot, pinned on both (`gradle.properties`, bumps: `skill.gtceu-upgrade`). Java only.

## In progress

- Project-wide improvement pass (2026-11-11): bug fixes, player text, README facts, small structure changes and the
  decisions the user left to me (wireless settings permission, stale channel endpoints, steam rule, Magical Assembler
  recipes under `miscRecipes`, unused jars removed from `libs/`), plus proxy pattern output marks, the fishing pond
  water culling fix (1.21) and Mekanism circuit tags at runtime. All in both worktrees, uncommitted (version still
  3.4.2); both compile, runData and the unit tests pass. `plan.improvement-2026-11` lists what was done, what was
  decided, and what is still open (structure proposals, small items, the parts no review read). In-game checks:
  `status.untested`.
- agent-toolkit (`E:\code\agent-toolkit`): two local commits on main are not pushed (the push was denied in the
  session); the user pushes them or allows it.

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (user: leave it; workaround is to not reload, restart
  if hit). `ClassCastException: IDrawable$2 (NONE) cannot be cast to UITexture` in `MachineUIPanel.<init>`. Cause:
  ModularUI 3.3.1-SNAPSHOT `ThemeManager` merges the java theme with a shallow `addAllOf`, then `parse` writes
  `background: "none"` into GTCEu's registered `GTGuiTheme` JSON (still so in MUI `3.3.1-20261013`). Possible fix:
  a `gtceufix/` client mixin passing `builder.getJson().deepCopy()`.
- Client crash in GTCEu's multiblock preview menu button (`AbstractMenuButton.checkClose`, ContextMenuButton), seen
  once (`run/crash-reports/crash-2026-11-07_00.46.34-client.txt`); cause unconfirmed, a gtceufix guard was proposed
  and not yet approved by the user.
- Upstream: on steam (BRONZE theme) panels the title and GT logo are pushed right and squeezed, GTCEu's own steam
  machines too.

## To do

- Untested changes to remind the user of (in-game checks): `status.untested`, each item linked to its `Untested:`
  comment in the code.
- Auto-build: substructures (`autobuild.*`). 1.21: once upstream ports GTCEu #5337, switch the settings window to
  GTCEu's multiblock preview and the planner to filtered predicates, as on 1.20 (`build.branch-differences`).
- Stack-like: external chemical hatches.
- Code review of 2026-11-08: done, see `plan.code-review-2026-11` for what is left;
  in-game checks in `status.untested`.
- FE output hatch (`wireless.fe`): no multiblock uses its ability yet, the user has later machines in mind.

## Open questions for the user

- Two stocking parts of one multiblock can't stock the same key (gtceu's rule, extended to ours by `StockingParts`).
  The user dislikes it but has no better idea: both would see the full network amount and the recipe check would
  count it twice.
