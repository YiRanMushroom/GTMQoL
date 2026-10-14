# Survival auto-build (`common/autobuild/`)

Split out of `context.md`. Both branches, config `misc.autoBuild`. The 1.20.1 code differences (`FriendlyByteBuf`,
Forge 47 events, no `MultiPredicate.getParent()`) are in `PORT_1_21.md`.

A terminal-icon button on every multiblock controller (`MachineUIPanelBuilderMixin`):

- left click places up to 64 blocks from the player's inventory;
- shift + left click toggles a job (64 blocks every 5 ticks);
- right click opens the settings window.

## Pieces

- `StructurePlanner`: GTCEu's structure helpers (anonymous subclasses override `getMinCount`/`getMaxCount`) with
  ability predicates at 0 (type name `Ability`/`TieredAbility`); ability-only positions are left empty. Sorted by
  y, then distance to the controller. Logs planning time.
- `AutoBuilder`: places only into air/replaceable blocks (anything solid counts as done), fires `EntityPlaceEvent`,
  calls `checkAndFormStructure` only at the end, and reports missing items. Jobs are keyed by dimension + controller
  pos. They stop when done, formed or stuck, when the player is away or offline, or when the controller is gone.
- `BuildSettings`: slice repeats, expandable bounds, block choices (pattern char → index in `root.expand()` +
  block). Transient client-side data per multiblock definition id (`AutoBuildPanel.SETTINGS`), sent with every
  click through a C2S `SyncHandler` (`AutoBuildButton.BuildRequest`). Only `DEFAULT_STRUCTURE`, no substructures.
- `BuildOptions`: what the window offers, computed the same way on both sides: slices with min < max, expandable
  bounds, and chars (not air/any/controller) whose non-ability predicates have at least 2 distinct blocks with an
  item. The server ignores choices not in it and clamps repeats/bounds.
  - A choice puts the `BlockInfo` into `getBlockPreferences()` keyed by the node that holds the predicate (GTCEu
    looks up by the node it walks, not the root). It gives the root's other non-ability predicates counts of 0.
- `AutoBuildPanel`: `IPanelHandler.simple` sub panel centered on screen. No synced values and no 3D preview
  (GTCEu's preview is buggy). Sliders + item toggle buttons; selecting a selected toggle goes back to the default.
  Expandable patterns only get size sliders for now.
  - The user doesn't want implementation details (tick counts, "kept until restart") in the UI text.
- `gtceufix/AbstractWidgetMenuMixin` (`PORT_1_21.md`): auto-build changes the structure errors while the UI is
  open, which hit a ModularUI menu bug.

## State

- Placing and the settings window work in game (1.21). `AbstractWidgetMenuMixin` is not verified.
- The 1.20.1 port is written, not built or run yet.
- Next: substructures, then block choices for expandable patterns.
