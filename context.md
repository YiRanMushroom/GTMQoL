# GTMQoL Current Context

Handoff for a new session: general working notes and what is open right now. Build environment and rules are in
`CLAUDE.md` (read it first). Design and how things work are in the doc index, not here.

## Branches

Two long-lived branches, kept in sync with cherry-picks. Features go on both unless marked 1.21 only.

| Branch | Version | Notes |
|---|---|---|
| `port/1.21.1` (this checkout, = `master` since PR #1) | NeoForge 21.1, Java 21 | Features are written here first |
| `migrate/gtceu-v8-runtime-generation` | Forge 47, Java 17 | ModDevGradle legacyForge; not run in game yet |

- The 1.20.1 worktree sits next to this one (`..\GTMQoL-1.20.1`). The user works on two computers, so paths differ
  (`C:\Users\yiran.wang\IdeaProjects\` or `E:\code\Minecraft\`): check `git worktree list` and the checked-out branch
  before porting.
- 1.21 only: MI gregification (MI has no Forge 1.20.1 build); KubeJS machines' late regeneration (modular and
  electric implosion copies of KubeJS recipes). Other differences: `port.differences-1.20.1`.
- GTCEu v8 snapshot, pinned on both (`gradle.properties`, bumps: `gtceu-upgrade.*`). Java only.
- `reference/` is the archived old implementation: research only, don't migrate it wholesale or modify it.

## Doc index

`index_open("<repo root>")`, then `index_find`. Never read `.agent-index.db` directly. Id prefixes:

| Prefix | Covers |
|---|---|
| `rules.*`, `build.*` | working rules, mixin conventions; build environment, dependencies |
| `layout.*` | source layout, where things live |
| `gregification.*` | foreign recipe types on GT machines (MI, Mekanism, Productive Bees, Create), composable single blocks |
| `stacklike.*` | stack-like recipe caps (Mekanism chemicals), AE bridges, universal ME parts, EMI/Jade |
| `ghost.*` | ghost ingredients, hatch, single block marks, MarkCoveredRecipes |
| `multiblock.*`, `autobuild.*` | SAF, DTFR, implosion, void miner, fishing pond, crystal growth, greenhouse; survival auto-build |
| `steam.*` | large steam multis, steam hatches, control circuits, early game |
| `ae2.*`, `wireless.*` | pattern buffers, shared hatches, smart doubling, dual/universal parts; wireless steam/EU, FE input |
| `recipedb.*`, `recipes.*`, `oc.*` | grouped search; magical assembler, recipes, circuits, tags, legacy molds; overclocking |
| `potions.*`, `modular.*` | potion fixes; modular multiblocks |
| `port.*`, `gtceu-upgrade.*` | gtceufix workarounds, 1.20.1 differences; bumping GTCEu |
| `config.*`, `generation.*`, `tests.*` | config, early config, datagen; runtime generation; integration tests |

## Known bugs, not fixed

- Client resource reload breaks GTCEu bronze/steel themed UIs (user: leave it; workaround is to not reload, restart
  if hit). `ClassCastException: IDrawable$2 (NONE) cannot be cast to UITexture` in `MachineUIPanel.<init>`. Cause:
  ModularUI 3.3.1-SNAPSHOT `ThemeManager` merges the java theme with a shallow `addAllOf`, then `parse` writes
  `background: "none"` into GTCEu's registered `GTGuiTheme` JSON. Possible fix: a `gtceufix/` client mixin passing
  `builder.getJson().deepCopy()`.
- Proxy pattern buffers don't get the pattern output marks for the recipe lookup (`ae2.*`).
- Upstream: steam panel title and logo squeezed (`steam.*`); GTCEu v8 terminal auto-build only works in creative,
  and only after the preview has been opened and closed once.

## To do

- Auto-build: substructures, then block choices for expandable patterns (`autobuild.*`).
- Crystal growth: a KubeJS binding for `CenterBlockCondition`.
- Stack-like: external chemical hatches.
- Agreed abstractions not done yet: a base for the mixin plugins' empty `IMixinConfigPlugin` methods; the cyclic
  multiblocks' popup helpers.
- Organize the index keys into trees (`index_keys`).

## Open questions for the user

- Two stocking parts of one multiblock can't stock the same key (gtceu's rule, extended to ours by `StockingParts`).
  The user dislikes it but has no better idea: both would see the full network amount and the recipe check would
  count it twice.
- Should the wireless accessor use the rainbow overlay?
- Maintenance hatches on our multis: all other abilities are used. Nothing removed yet.

## Recent

- 2026-10-20: `docs/*.md` retired into the doc index; context trimmed. Both branches: shared hatches gating fix,
  block provider lookup fix, greenhouse batch mode; 1.20.1 got the potion "any mod's version" hint.
