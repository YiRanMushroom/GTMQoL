# GTMQoL

A personal quality-of-life addon for GregTech CEu Modern v8.

| Minecraft | Loader | Branch | Recipe viewer |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1 | `1.21` | EMI |
| 1.20.1 | Forge 47 | `1.20` | JEI or EMI |

Both branches have the same features, except the few marked 1.21.1 only. Releases are `v<version>-1.21` and
`v<version>-1.20`.

## Features

### Wireless networks

- A global steam pool and a global EU pool per network. A network belongs to the player's FTB team, or to
  the player alone when FTB Teams isn't installed. Each player also has a separate private network.
- Machines are bound to whoever places them. They can be rebound in the UI, or by copying the binding with a
  data stick (Shift-right-click to copy, right-click to paste).
- Steam: wireless steam input/output hatches, an accessor block (fluid handler on all sides) and a monitor
  that shows the stored steam and the input/output rates.
- EU, one per voltage tier: wireless energy input/output hatches, which also work as substation and laser
  hatches, and an accessor. There is also one monitor for all tiers.
  - Amperage can be set per machine, from 1 to 2^24.
  - `/gtmqol default_amperage [amperage]` sets the default for machines you place.
  - Input hatches have an overclock toggle: one tier higher voltage, at 4× the energy cost.
- Wireless covers (steam and EU, input and output) connect any machine to the network.
- Recipes are in the magical assembler.

### Energy

- Every GT machine and cable that accepts EU also accepts Forge Energy (FE).
- EU → FE uses GTCEu's own conversion.

### Magical assembler and circuits

- The magical assembler: a tiered machine with 16 item and 4 fluid inputs that makes most of this mod's items.
- Universal circuits, one per tier, which count as any circuit of that tier. AE2 patterns encode them in
  place of a specific circuit.
- GT circuit tags and Mekanism's circuit tags are merged (LV/MV/HV/EV ↔ basic/advanced/elite/ultimate).
  ULV circuits are merged with infused alloy.
- Material name aliases: tags such as `c:ingots/aluminum` include GT's `aluminium`, so recipes from other mods
  accept GT materials.

### Modular machines

Every tiered single-block machine also gets a 3×3×3 modular multiblock, including machines from other
addons. It can be built from any blocks and takes any hatches except parallel hatches. Recipes run 8×
faster, with perfect sub-tick overclocking and batch mode. Generator recipes last 8× longer instead, and
generators use their own overclock.

### Overclocking

- Non-perfect overclocking costs 4× EU/t for 4× speed. Perfect overclocking costs 2× EU/t for 4× speed.
- Both run sub-tick: once a recipe reaches one tick, further overclocks become parallels.
- No ULV overclock penalty.
- Coil multiblocks: every overclock costs 4× EU/t for 8× speed, regardless of coil temperature.
- The recipe viewer shows the same numbers.
- The multi smelter applies overclocking after parallels, so its results stay correct.
- Optional, on by default:
  - Fusion reactor buff: perfect sub-tick overclocking, the full hatch voltage instead of the tier cap, and
    substation and laser hatches in the energy slots.
  - Multi-tier skipping: a multiblock with several energy hatches runs at the voltage of all hatches summed.
  - Absolute parallel hatches: parallels from a parallel hatch don't raise EU/t, so a multiblock runs as many as
    the hatch allows at the EU/t of one recipe. Generators are not affected.

### Multiblocks

- **Smart Assembly Factory**: runs assembly line recipes, with parallel hatches, perfect sub-tick
  overclocking and batch mode.
- **Dimensionally Transcendent Fusion Reactor**: runs fusion recipes of any tier, without the start energy
  requirement.
- **Electric Implosion Compressor**: every TNT implosion recipe without the explosive, with parallel hatches
  and perfect sub-tick overclocking. Nether star dust can be mixed from diamond and silver dust at HV, so
  nether stars are available before IV. Vanilla TNT crafting is kept.
- **Void Miner**: mines the ores of its dimension's GT ore veins, weighted like the veins, with no energy
  hatch: it draws EU from the wireless network. The number of ore types and stacks per cycle can be set.
- **Industrial Fishing Pond**: a water-filled pond that rolls vanilla fishing loot with the fishing rod placed
  in it (Lure and Luck of the Sea count). Normal or treasure mode, up to 64 rolls per cycle, and the output
  scales with the energy put in.
- **Crystal Growth Chamber**: grows shards from the budding block in its center: amethyst, certus quartz and
  any mod's `budding_<x>` block with an `<x>_shard`.
- **Greenhouse** (single block, every tier) and **Industrial Greenhouse** (IV multiblock, 16× outputs, parallel
  hatches): grow crops and trees from a seed or sapling and water. Mystical Agriculture crops are supported,
  and other mods' saplings and crops are picked up by name.

- **Industrial Large Chemical Reactor**: large chemical reactor recipes 64× faster, with parallel hatches,
  perfect sub-tick overclocking and batch mode.
- **Probable Certainty Device** (ZPM) and **Probable Improbability Device** (IV): parts that fit most multiblocks
  (wherever their buses and hatches go). Certainty: chanced outputs always come out, and ranged outputs give their
  largest amount. Improbability: chanced inputs are never consumed, and ranged inputs use their smallest amount.

Unlike GTCEu's, the void miner and the fishing pond never void their outputs: they wait until everything fits.

### Ghost ingredients

A ghost ingredient tells a machine which recipe to run when its inputs fit several, for example planks or stairs
from the same log. It is never used up.

- The **Ghost Ingredient Hatch** holds a list of marks (click or drag items and fluids from the recipe viewer).
  It counts as an input bus, so any multiblock takes it.
- Single blocks that need marks get a button for them in their UI.
- Recipes that need a mark show it in the recipe viewer.

### Advanced steam multiblocks and steam tweaks

- Large steam versions of 21 machines (macerator, compressor, forge hammer, extractor, alloy smelter,
  furnace, bender, wiremill, lathe, cutter, extruder, forming press, mixer, centrifuge, thermal centrifuge,
  ore washer, chemical bath, sifter, assembler, circuit assembler, magical assembler). The structures are
  GTNL's large steam multiblocks built from bronze blocks (the assembler and the magical assembler use GTNL's
  steam manufacturer). One steam hatch; steam buses or regular buses and hatches of any tier.
- Recipes up to MV (the magical assembler: LV) run as if overclocked (non-perfect) to that tier, then take
  80% of the time and 75% of the steam per tick.
- 16 parallels. A Steam Parallel Control Hatch sets it anywhere from 16 to 256.
- A low/high pressure steam magical assembler, which makes all of these.
- Control circuits, ULV to EV, made cheaply in the circuit assembler. The large steam circuit assembler can
  make ULV to HV.
- Steam single blocks get fluid input/output tanks and a ghost circuit slot; steam input buses get one too.
- No output limits on the steam macerator and similar machines, so byproducts always come out.
- The large boiler's muffler and maintenance hatch are optional.
- Early game: simple bus and hatch recipes from machine casings, sand still smelts into glass, and manual
  compression (3×3 blocks, nuggets) is kept.

### AE2

- **Overclocked ME Pattern Buffer**: 216 patterns instead of 27.
- **ME processing**: an ME assembler and an ME circuit slicer, with GT-style recipes for AE2 processors and
  for GTCEu's ME parts.
- **ME dual input/output parts**: item and fluid stocking in one part, item and fluid output in one part.
- **Universal ME input/output parts**: any AE key the mod knows, including Mekanism chemicals.
- **Pattern buffer return**: a multiblock's outputs go straight back into the ME network for waiting crafting
  jobs. Toggle per buffer.
- **Sticky card**: a storage bus upgrade. Items that match its partition only go into that storage bus.
  ExtendedAE's storage buses work too.
- Pattern encoding prefers universal circuits and skips inputs that a recipe doesn't consume.
- **Smart doubling** on pattern buffers when ExtendedAE Plus is installed, with an on/off switch and a limit.
- **Shared hatches** (toggle per pattern buffer): a pattern can use circuits, molds and ghost marks from the
  machine's other, undyed buses and hatches.
- **Mark pattern outputs** (toggle on the overclocked pattern buffer): each pattern counts as a ghost mark for its
  own output, so the machine picks the recipe the pattern is for.

### Recipe lookup performance

GTCEu's recipe search tries every combination of a machine's inputs. With many distinct buses or pattern
buffers this gets very slow in the late game. Grouped search only tries combinations that one bus, one
pattern (plus the buffer's circuit and shared inventory) or the non-distinct pool could supply, which are
the only ones GTCEu would run anyway. Technical details are in the source, `common/recipedb/`.

### Mekanism chemicals

Mekanism chemicals are a real GT recipe capability. Chemicals go in and out through ME parts and pattern
buffers (with Applied Mekanistics), show up in EMI recipes, and Jade shows them in a machine's current recipe.

### Gregification

Other mods' machine recipes run on GT machines and multiblocks, with GT overclocking, parallels and batch mode.
Each machine is crafted from the original mod's machine.

- **Modern Industrialization** (1.21.1): 14 single blocks and 10 multiblocks, including the blast furnace with GT
  coils.
- **Create**: 12 multiblocks of Create casings: millstone, crushing wheels, press, compactor, mixer (heated and
  superheated recipes run at higher tiers), encased fan (every processing kind, also from addons), deployer,
  spout, item drain, saw, mechanical crafter and a sequenced assembly line (3 to 16 slices long).
- **Mekanism**: 23 multiblocks, from the crusher and enrichment chamber to the chemical machines, the rotary
  condensentrator and the thermal evaporation plant. They take any input/output parts.
- **Productive Bees**: comb centrifuge, incubator, gene extractor, beehive (bees in cages, with upgrades and
  flowers) and breeder. The GT canner turns caged bees into spawn eggs and back, keeping their genes.

Gregified single blocks have batch mode, and ghost marks where their recipes need them.

### Survival auto-build

A button on every multiblock controller builds the structure from the player's inventory: a click places up to
64 blocks, Shift-click keeps building until the structure is done. Right click opens a window for the structure's
size and block choices (for example the coil type).

### Potions

- GTCEu's brewery and canner work with potions (outputs keep their potion, EMI finds the recipes).
- Splash and lingering potions are brewed in the brewery and bottled in the canner.
- Potion fluids are named after their potion, including strength and duration.

### Other recipes

- **Legacy part recipes**: extruder molds for rods, long rods, bolts, gears, small gears and rotors, which GTCEu
  v8 removed.
- GT wires count as other mods' wires (`c:wires/<material>`, `forge:` on 1.20.1), so their recipes take GT wires.

### GTCEu fixes

Workarounds for GTCEu bugs, removed once fixed upstream. Each branch has the ones its GTCEu needs. For example:
pattern buffers failing recipes that need more than one of an item, chanced outputs lost in very large batches,
machines going idle after repeating a recipe, a costly energy check on high-amperage energy lines, the ME stocking
bus UI not updating, the multiblock preview drawn in the wrong place in the recipe viewer, raw recipe category
names, and mod loading crashes with some modpacks.

## Configuration

`config/gtmqol.yaml`. Every feature has a toggle, on by default; options need a restart. The full list is in
the config file itself.

| Group | Options |
|---|---|
| `machines` | one per multiblock above, `probableDevices`, the advanced steam machines and `easySAFRecipe` |
| `modularMachines` | `enabled` |
| `gregification` | `modernIndustrialization` (1.21.1), `mekanism`, `productiveBees`, `create` |
| `wireless` | `energy`, `steam` |
| `circuits` | `universalCircuits`, `controlCircuits`, `mekanismCircuitTags` |
| `recipes` | `miscRecipes`, `earlyGame`, `keepManualCompression`, `keepVanillaTNT`, `netherStarDust`, `useLegacyPartRecipes` |
| `steamTweaks` | `circuitSlots`, `fluidTanks`, `noOutputLimits`, `optionalLargeBoilerParts` |
| `overclocking` | `buffFusionReactor`, `enableMultiTierSkipping`, `absoluteParallelHatches` |
| `ae2` | `overclockedPatternBuffer`, `processing`, `dualHatches`, `patternBufferReturn`, `skipNotConsumedInputs` |
| `misc` | `feInput`, `autoBuild` |
| `voidMiner` | `dimensionMapping`: mine another dimension's veins (`"from=to"`) |
| `integrationTests` | `enabled` (default off): runtime-generated example machines, for development |

`config/gtmqol-early.properties` holds options that are read before mods load (needs a restart):

| Option | Default | Description |
|---|---|---|
| `recipeDB.groupedSearch` | `true` | Grouped recipe search |
| `overclocking.overhaul` | `true` | The overclocking changes above |
| `ae2.universalCircuitEncoding` | `true` | Patterns encode universal circuits |
| `ae2.stickyCard` | `true` | The sticky card |

## Optional dependencies

None are required.

- FTB Teams: shares wireless networks per team.
- AE2: the AE2 features above, and pattern buffers split by pattern in the grouped recipe search.
- ExtendedAE: the sticky card works on its storage buses.
- ExtendedAE Plus: smart doubling on pattern buffers.
- Mekanism: chemicals as a recipe capability, Mekanism gregification, merged circuit tags.
- Applied Mekanistics: chemicals in ME parts and pattern buffers.
- Modern Industrialization (1.21.1): MI gregification.
- Productive Bees: Productive Bees gregification.
- Create: Create gregification.
- Mystical Agriculture: greenhouse recipes for its crops.
- Jade: chemical outputs in the machine overlay.
- EMI or JEI (1.20.1): chemicals, potions and ghost ingredients in recipes, the pattern encoding tweaks.
- KubeJS: the block and ghost ingredient recipe capabilities in recipe scripts.

## License

Copyright (C) 2026 Yiran. Licensed under the GNU Lesser General Public License v3.0, see
[LICENSE](LICENSE).

## Credits

- The wireless network (storage shared per player or team, data stick binding, the monitor) is inspired by
  [GTMThings](https://github.com/liansishen/GTMThings).
- The advanced steam multiblocks and the control circuits (items and recipes) are based on
  [GT Not Leisure](https://github.com/Darknight2333/GT-Not-Leisure1) (GTNL), a GregTech: New Horizons
  addon. The control circuit textures are taken from GTNL and are licensed under LGPL-3.0.
- The animated rainbow overlay on the wireless hatches (`overlay_wireless.png`) is taken from GTMThings'
  `overlay_energy_on_wireless`, which originally comes from GregTech: New Horizons.
