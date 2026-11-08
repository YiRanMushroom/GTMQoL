+++
title = "Code review 2026-11-08: what was done (performance, UI, mixins, structure) and what is left"
kind = "status"
keys = [ "improvement plan", "package structure", "performance", "refactor", "shift drag" ]
updated = "2026-11-08 19:35:14"
checked = "2026-11-08"
+++
Done on both branches (2026-11-08), each spot with an Untested: note in status.untested:
- Performance: CraftingCpuLogicBatchMixin skips patterns that can't do one craft in a pass and bounds the partial-push search; PatternBatch builds its inputs/outputs once; no per-tick markAsChanged in the matrix and cyclic machines (GTCEu syncs only dirty fields, saves every tick); matrix decodes only changed pattern slots and skips AE2 updates when nothing changed; DecodedPatterns caches decoded patterns per recipe manager; matrix pays FE once per tick, each wireless network counted once (fixes unpaid crafts); refused outputs retried every 20 ticks; CraftingPatternDetails.isValid without a stack per call.
- Patterns window: icons sent by the server, entries of unchanged slots reused, immutable sync copies, tooltip cached per hovered pattern. Shift-drag: ShiftDragQuickMove (client listener, no mixin).
- Mixins 87 -> 76 (1.20: 84 -> 72): brewery/canner -> recipe logic runners, output limits -> SteamTweaks at setup, recipe viewer layouts -> RecipeTypeLayouts, steam constructor mixins -> MachineFeatures (api.machine-features), merges (EMI, JEI on 1.20, shared hatches, MachineBuilderAccessor). AE2MixinPlugin names switched mixins only.
- Structure: rules.layout rule; renders, recipes, tags moved into their features; integration/ae2 split (patternbuffer, parts, stickycard, crafting); stacklike AE code under common/stacklike/ae2.

Left, by choice: mixin subpackages per feature (configs/plugins churn for little gain); a patterns window sync of changed slots only (current one holds thousands); pattern buffer toggles as one configurator trait (saved keys stay in the mixins).