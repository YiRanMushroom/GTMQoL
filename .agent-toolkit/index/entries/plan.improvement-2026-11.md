+++
title = "Project-wide improvement pass (2026-11-11): what was done, and the reviewed findings still open"
kind = "status"
keys = [ "audit", "improvement plan", "readme outline", "review findings", "text audit" ]
updated = "2026-11-11 08:20:07"
checked = "2026-11-11"
+++
The user asked for an open, PM-level pass over the whole project. Four read-only reviews ran (common/api/config; player text and README; AE2 + stack-like + crafter; gregification + wireless + mixins, the last two with a budget, so not everything was read: see "Not reviewed"). Everything below "Done" is in both worktrees, compiles, passes runData and the unit tests; in-game checks are in status.untested.

## Done
- Non-null defaults in every package (rules.working), dead code and unused imports removed, ghost ingredient types registered explicitly.
- Bugs: tags held optional items as required entries (GTMQoLAddon.optionalItemTag / optionalBlockTag); perfect overclock gave no overclock for recipes of 128-255 ticks one tier up (negative shift) and equivalentOC could slow a recipe down below its tier; HNN simulation looped once per craft (billions) and its craft count wrapped (Overclocking.crafts saturates, also for PatternWear); ME Universal / Dual Input could take the newly pasted key for a recipe matched on the old stock; Assembling Matrix: cancelled inputs were held back by the retry cooldown (lost when the controller broke), a non-wearing Crafting Pattern job above 2^31 crafts never started, patterns weren't decoded again after /reload; pattern core's onMachineDestroyed skipped super; ME dual output buffer kept a null key of a removed mod; stack-like inputs weren't simulated against their storage; wireless energy hatch window re-applied the values from when it was opened; Wireless Energy Cell's 1e30 maximum swallowed other cells' in AE2's double sum (now 1e19, current capped there); MI converter casts saturate; ILCR tooltip's raw key (1.21); Void Miner needs wireless.energy; ElectricImplosion hook on the main thread; ModularMachines queue and GTMQoLConfig.get() safe for parallel mod construction.
- Removed: the dead HEAD inject of NetworkStorageMixin. (circuits.mekanismCircuitTags was removed and came back with the runtime tags, see Decided.)
- Structure: config.wirelessFE() / assemblingMatrix() for the compound gates; circuit conversion recipes in UniversalCircuits; client level/executor helpers in GTMQoLClient; pattern buffer return lang always added, from its own class.
- Player text: "ME Assembler" / "ME Circuit Slicer" names (registered through tieredMachine), modular machines named after their single block with a tooltip, tooltips for SAF, Electric Implosion Compressor, ME Dual Output, parallelizable lines, binding line on FE machines and the energy cell, Assembling Matrix FE line read live; one wording for the wireless overclock cost; Sticky Card, fishing pond, DTFR, toggle and channel texts rewritten; the vocabulary in skill.naming-and-text; mods.toml description; config comments; README facts (fishing pond is built empty, FE direction, toggles, machines keys, optional mods).
- Instructions: rules.direction, skill.release. 1.20 pitfall: build.early-class-load.

## Decided 2026-11-11 (the user: decide these as you see fit) and done, both branches
- Wireless settings (amperage, overclock, cube sides, steam accessor auto output) go through WirelessBindingTrait.checkSettings: anyone while unbound, then everyone on the machine's network (bound player, their team in team mode) and operators. Not owner-only: a team shares its machines.
- Stale channel endpoints: before ChannelSavedData.bind refuses with "router needed" it drops endpoints whose position is loaded and holds no machine bound to the channel (isGone). An unloaded stale one still blocks; deleting the channel is the way out.
- Steam rule: anything in GT's steam tag goes in, GT steam comes out (WirelessSteamTank.isSteam); the output steam cover now drains by the tag too.
- MagicalAssembler's creative data access hatch (make, copy) and rubber sapling recipes are under recipes.miscRecipes.
- The six unused jars in libs/ are removed (git rm; libs/ keeps what build.gradle names).
- Mekanism circuit tags at runtime, option back (recipes.circuit-tags); the rule is in rules.direction.
- Proxy pattern buffers get the pattern output marks (ae2.pattern-output-marks); fishing pond water culling (gtceufix.ber-render-box, 1.21).
- AEProcessing's ME assembler recipes for GT's ME parts stay next to GTCEu's own assembler recipes (the user: keep).
- Not doing: config key renames (they reset players' values for a nicer name), the README as a product page (the user saw no need), renaming the gregified machines' mod abbreviations and the control circuit tier names (GTNL's).

## Open: structure
- Recipes are gated twice, in GTMQoL (init) and GTMQoLAddon.addRecipes; a mismatch is a null recipe type. Proposal: a feature's init registers its recipe callback. An abstraction: propose first.
- GTMQoLMultiblocks holds the void miner's and fishing pond's definition, lang and recipe: common/voidminer and common/fishingpond like the other features.
- The recipe viewer column layout exists three times (BlockProviderRecipeViewer, GhostIngredientRecipeViewer, StackLikeRecipeViewer); the "text button toggling a popup" four times (PopupWidgets.openButton); the data stick delegation in every wireless machine (an interface with default methods).
- FusionReactorMachineMixin's <clinit> wrap is unneeded: the two getModifier wraps could pass PERFECT_OVERCLOCK_SUBTICK themselves.
- rules.layout: say that a single class needing an optional mod stays in the feature root.

## Open: small
- IndustrialFishingPondMachine rescans all parts for energy containers every working tick: cache at formStructure.
- markAsChanged() for @SaveField-only state sends an empty client update (GhostIngredientHandler, whose comment is wrong, VoidMinerMachine, fishing pond, PendingOutputTrait, CyclicMultiblockMachine, BatchModeTrait).
- BuildOptions.isAbility recomputed per predicate lookup; constant ModifierFunctions rebuilt per call (Greenhouse, GTMQoLMultiblocks ILCR, ModularMachines).
- Tooltip texts repeat constants as literals (void miner EU per stack and cycle, pond numbers, ILCR 64).
- CraftingSolver runs inside one AE2 time slice (no handlePausing): only if a profile shows it. FluidInput.getPossibleInputs allocates per call. AssemblingMatrixWork:169 uses the first input's amount as perUse for every key.
- Auto build settings: "Size %s", "Block %s" are bare indexes. Config screen labels are probably raw keys (GTCEu generates config.screen.* lang, we don't): check in game.
- Covers do a capability lookup on their own block every tick (BlockCapabilityCache), only if covers show in a profile.
- A converted recipe with duration 0 would divide by zero in GregificationModifiers (Mekanism values pass through unclamped): not confirmed.

## Not reviewed
common/crafter (CraftingPattern, behaviour, UI), CraftingSolver, AEProcessing, the stack-like root and mekanism bridge, most ae2 mixins beyond the batching and storage ones, the gregification converters' bodies, MultiblockShape, all wireless UI classes and monitors, the channel UI, the mixins GTMultiMachinesMixin, GTMachineUtilsMixin, GTSingleblockMachinePanelsMixin, MachineUIPanelBuilderMixin, the recipe/potion ones and gtceufix/.