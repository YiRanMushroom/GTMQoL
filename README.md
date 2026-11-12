# GTMQoL

A personal quality-of-life addon for GregTech CEu Modern v8. In game it is listed as **GTM Quality of Life**.

| Minecraft | Loader | Branch | Recipe viewer |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1 | `1.21` | EMI |
| 1.20.1 | Forge 47 | `1.20` | JEI or EMI |

Both branches have the same features, except the few marked 1.21.1 only. Releases are `v<version>-1.21` and
`v<version>-1.20`; each release's notes name the GTCEu build it needs.

## Features

### Crafting and AE2

#### Pattern Crafter

The **Pattern Crafter** (single block, every tier) runs crafting table, stonecutter and smithing table recipes.

- Right-click a **Blank Crafting Pattern** to encode a recipe from a grid of ghost items (dragged from EMI/JEI or
  filled with their transfer button). Put the **Crafting Pattern** in the machine with the materials; the machine
  holds 9 patterns.
- A Crafting Pattern matches its items exactly, or accepts whatever the recipe accepts. It can take buckets from the
  machine's tanks as fluid, and can keep an item the recipe duplicates (smithing templates), so one is enough.
- Tools stay in the machine and wear out by one craft per parallel.
- AE2 crafting, stonecutting and smithing patterns work in the Pattern Crafter too.
- A Crafting Pattern whose recipe was changed or removed by a recipe script says so in its tooltip.
- Holding Shift shows a Crafting Pattern as the item it crafts, like AE2's patterns.

#### Assembling Matrix

The **Assembling Matrix** is a multiblock that crafts for its ME network in bulk: AE2 crafting, stonecutting and
smithing patterns, and the Crafting Patterns made for the Pattern Crafter. Processing patterns are not crafted here.

- **Structure**: a box of 3 to 15 blocks each way, built like the cleanroom, with the controller in the middle of the
  front's bottom edge. The edges are Machine Casings, all of one tier; the faces are Quartz Glass; the inside holds
  **Assembling Matrix Processing Cores**, **Assembling Matrix Pattern Cores** or air. At least one of each core is
  needed to craft anything, so the smallest working box is 3 × 3 × 4.
- **Speed**: each Processing Core does 4 crafts per tick, ×4 for each casing tier above ULV. The structure preview
  shows ULV casings, but any tier works. Every craft costs 64 FE, taken from Wireless FE Input Hatches on the edges.
- **Patterns**: each Pattern Core holds 72 patterns. The controller's patterns window lists them all, with a search
  field; patterns are taken out with a click and put in from the cursor or with Shift-click from the inventory.
- **Parts on the edges**: one **Assembling Matrix ME Interface**, at least one Wireless FE Input Hatch, and Input
  Buses for catalysts. Catalysts are the tools and smithing templates that a Crafting Pattern keeps instead of using
  up; they stay in the Input Buses, and tools wear down there.
- **Auto upload**: Shift-clicking Encode in a Pattern Encoding Terminal on the same ME network moves a crafting,
  stonecutting or smithing pattern right into the Assembling Matrix; a plain click keeps it in the terminal as usual.
  A notification in the top right corner, visible over the terminal, says what happened: a pattern the matrix already
  has, or one no matrix has room for, stays in the terminal. The switch is on the controller and on the ME Interface.
- **ME Interface**: its name, shown in Pattern Access Terminals, can be changed in its window.
- **Cancelling**: AE2's cancel stops the crafting job, but crafts already sent to the matrix still finish and their
  products go to the network. To get the ingredients back instead, cancel the job in the Crafting Status first, then
  Shift-click the return button on the controller: the ingredients of all crafts not done yet go back to the ME
  network, exactly as they were sent.
- **Breaking and rebuilding**: a broken structure keeps its crafts and resumes when formed again. Breaking the
  controller sends the ingredients of unfinished crafts and the finished products to the ME network; whatever the
  network can't hold is lost.
- **Recipes**: the cores and the ME Interface in the ME Assembler (the assembler without `ae2.processing`); the
  controller is crafted from an IV Pattern Crafter, an IV Machine Hull, 64k Crafting Storage, IV Robot Arms, IV
  circuits and a Crafting Co-Processing Unit.
- Needs AE2, and the config switches `machines.patternCrafter`, `wireless.energy` and `wireless.fe` on (all on by
  default).

#### Pattern Batching

With **Pattern Batching**, a crafting job sends many crafts of a pattern to a machine at once, as one batch, instead
of one craft at a time. Big orders finish in far fewer steps and need fewer Crafting Co-Processing Units.

- **ME Pattern Buffers** (GTCEu's and the Overclocked ME Pattern Buffer): the ×2 button opens the Pattern Batching
  settings, an on/off switch (on by default) and a batch limit, the most items or buckets of fluid of any one
  ingredient in a batch (0 = no limit). Only processing patterns are batched.
- **Assembling Matrix**: always takes batches, except for Crafting Patterns whose catalyst wears out.
- **How orders are cut**: a pattern whose ingredients are all in storage goes out as one batch. A pattern whose
  ingredients are still being crafted goes out in batches of 1, 2, 4 and so on, so the next step of a chain starts as
  soon as the first items arrive.
- **AE2's own Crafting CPUs**: the batches of one pattern cost one co-processing operation together. A batch that
  can't go whole, because some ingredients are still on their way or the ME network lacks power, sends the part that
  can go now and keeps the rest for later. Other mods' crafting CPUs get the batches too, without these extras.
- **Not batched**: patterns that give something back (an empty bucket, for example) or make one of their own
  ingredients, and patterns that a plain Pattern Provider also offers.
- Running crafting jobs keep their batches over a restart.
- `ae2.patternBatchParts` (default 1): above 1, the largest batch is about this fraction of the order, so the next
  step of a chain starts even sooner, at the cost of more steps.

#### Faster crafting plans

A crafting request is worked out in one go, however large the amount, so huge orders no longer stall on
"Calculating". When an item has several patterns, the plan moves to the next one where the first runs short.
Requests that need AE2's own rules (patterns that give items back, patterns that loop, substitutes in short supply)
are still worked out by AE2. In rare cases with several patterns for an item, a request that AE2 could plan shows as
missing items; switching `ae2.fastCraftingPlans` off brings back AE2's own planning, no restart needed.

#### ME parts and pattern buffers

- **Overclocked ME Pattern Buffer**: 216 patterns instead of 27.
- **ME processing**: an ME Assembler and an ME Circuit Slicer, with GT-style recipes for AE2 processors and for
  GTCEu's ME parts.
- **ME dual input/output parts**: item and fluid stocking in one part, item and fluid output in one part.
- **Universal ME input/output parts**: any AE key the mod knows, including Mekanism chemicals.
- **Pattern buffer return**: a multiblock's outputs go straight back into the ME network for waiting crafting jobs.
  Toggle per buffer.
- **Shared hatches** (toggle per pattern buffer): a pattern can use circuits, molds and ghost ingredients from the
  machine's other, undyed buses and hatches.
- **Mark pattern outputs** (toggle on the Overclocked ME Pattern Buffer): each pattern counts as a ghost ingredient of
  its own output, so the machine picks the recipe the pattern is for. Works through an ME Pattern Buffer Proxy too.
- **Sticky card**: a storage bus upgrade. Items that match its partition only go into that storage bus.
  ExtendedAE's storage buses work too.
- Pattern encoding prefers universal circuits and skips inputs that a recipe doesn't consume.

### Wireless networks

- A global steam pool and a global EU pool per network. A network belongs to the player's FTB team, or to
  the player alone when FTB Teams isn't installed. Each player also has a separate private network.
- Machines are bound to whoever places them. They can be rebound in their window, or by copying the binding with a
  Data Stick (Shift-right-click to copy, right-click to paste).
- A bound machine's settings (amperage, overclock, an energy cube's sides) can be changed by the players on its
  network and by operators; only the bound player or an operator can unbind it or switch its network.
- Steam: wireless steam input/output hatches (the input hatch also works as a fluid input hatch, for recipes that
  use steam), an accessor block (fluid handler on all sides) and a monitor that shows the stored steam and the
  input/output rates.
- EU, one per voltage tier: wireless energy input/output hatches, which also work as substation and laser
  hatches, and an accessor. There is also one monitor for all tiers.
  - Amperage can be set per machine, from 1 to 2^24.
  - `/gtmqol default_amperage [amperage]` sets the default for machines you place.
  - Input hatches have an overclock toggle: they run one tier higher, and every EU then costs 4 EU from the network.
- FE, from the EU in the network, with no rate limit and no storage of their own:
  - The **Wireless Energy Cube**: each side is set to input, output or off, like Mekanism's.
  - **Wireless FE Input and Output Hatches**, for multiblocks that use or make FE.
  - With AE2, the **Wireless Energy Cell** powers an ME network from the wireless network. It only gives power:
    other power sources on the ME network can't fill the wireless network.
- Wireless covers (steam and EU, input and output) connect any machine to the network.
- The steam network takes any fluid in GT's steam tag and gives back GT's steam.
- **Channels** link research machines across any distance and dimension, while both ends are loaded. A channel is
  private, shared with the FTB team, or public, and is created and bound in the machine's window.
  - Computation: **Wireless Computation Transmission and Reception Hatches** link one transmitter to one receiver.
    A **Wireless Computation Network Router** bound to the channel allows any number of each.
  - Optical data: **Wireless Optical Data Transmission and Reception Hatches**. A reception hatch gives its assembly
    line the research of every data bank on its channel.
- Recipes are in the magical assembler.

### Energy

- Every GT machine and cable that accepts EU also accepts Forge Energy (FE).
- FE is converted at GTCEu's `feToEuRatio`.

### Magical assembler and circuits

- The magical assembler: a tiered machine with 16 item and 4 fluid inputs that makes most of this mod's items.
  With `recipes.miscRecipes` it also makes GTCEu's Creative Data Access Hatch (ZPM; one hatch then copies itself
  onto Advanced Data Access Hatches) and turns any sapling and a Sticky Resin into a Rubber Sapling.
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
generators use their own overclock. What a recipe type needs from its machine carries over: the modular pattern
crafter wears tools, the modular simulation chamber trains data models.

### Overclocking

- Non-perfect overclocking costs 4× EU/t for 4× speed. Perfect overclocking costs 2× EU/t for 4× speed.
- Both run sub-tick: once a recipe reaches one tick, further overclocks become parallels.
- No ULV overclock penalty.
- Coil multiblocks: every overclock costs 4× EU/t. For each 900 K the coils are above the recipe's temperature, one
  overclock gives 8× speed instead of 4×; GTCEu's 5% EU/t discount per 900 K stays.
- Research: every overclock halves the computation (CWU) a research recipe needs.
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
  overclocking and batch mode. It takes up to 9 Data Access Hatches and Optical Data Reception Hatches.
- **Dimensionally Transcendent Fusion Reactor**: runs fusion recipes of any tier, without the start energy
  requirement.
- **Electric Implosion Compressor**: every TNT implosion recipe without the explosive, in 4× the time, with
  parallel hatches and perfect sub-tick overclocking. Nether star dust can be mixed from diamond and silver dust at HV, so
  nether stars are available before IV. Vanilla TNT crafting is kept.
- **Advanced Chip Engraving Factory**: every laser engraver recipe without the lens and without a cleanroom or any
  other condition; engraving a wafer takes 4× the time for 4× the output. Parallel hatches, overclocking and batch mode like GTCEu's Large Engraving Laser. Mark the
  product in a Ghost Ingredient Hatch, or turn on Mark pattern outputs on an Overclocked ME Pattern Buffer. The
  controller is made in the magical assembler from a Large Engraving Laser, ZPM circuits and IV emitters.
- **Void Miner**: mines the ores of its dimension's GT ore veins, weighted like the veins, with no energy
  hatch: it draws EU from the wireless network. The number of ore types and stacks per cycle can be set.
- **Industrial Fishing Pond**: a basin of Watertight Casings, built empty (the machine shows the water itself),
  that rolls vanilla fishing loot with the fishing rod placed in it (Lure and Luck of the Sea count). Normal or treasure mode, up to 64 rolls per cycle, and the output
  scales with the energy put in.
- **Crystal Growth Chamber**: grows shards from the budding block in its middle: amethyst, certus quartz and
  any mod's `budding_<x>` block with an `<x>_shard`.
- **Greenhouse** (single block, every tier) and **Industrial Greenhouse** (IV multiblock, 16× outputs, parallel
  hatches): grow crops and trees from a seed or sapling and water. Mystical Agriculture crops are supported,
  and other mods' saplings and crops are picked up by name.
- **Industrial Large Chemical Reactor**: large chemical reactor recipes 64× faster, with parallel hatches,
  perfect sub-tick overclocking and batch mode.
- **Probable Certainty Device** (ZPM) and **Probable Improbability Device** (IV): parts that fit most multiblocks
  (wherever their buses and hatches go). Certainty: chanced outputs always come out, and ranged outputs give their
  largest amount. Improbability: chanced inputs are never consumed, and ranged inputs use their smallest amount.
- The Assembling Matrix is described under Crafting and AE2.

Unlike GTCEu's multiblocks, the Void Miner and the Industrial Fishing Pond never void their outputs: they wait
until everything fits.

### Ghost ingredients

A ghost ingredient tells a machine which recipe to run when its inputs fit several, for example planks or stairs
from the same log. It is never used up.

- The **Ghost Ingredient Hatch** holds a list of ghost ingredients (click with an item, or drag items and fluids
  from the recipe viewer). It counts as an input bus, so any multiblock takes it.
- Single blocks whose recipes need ghost ingredients get a button for them in their window.
- Recipes that need a ghost ingredient show it in the recipe viewer.

### Large Steam multiblocks and steam tweaks

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

### Recipe lookup performance

GTCEu's recipe search tries every combination of a machine's inputs. With many distinct buses or pattern
buffers this gets very slow in the late game. Grouped search only tries combinations that one bus, one
pattern (plus the buffer's circuit and shared inventory) or the non-distinct pool could supply, which are
the only ones GTCEu would run anyway.

### Mekanism chemicals

Mekanism chemicals are a real GT recipe capability. Chemicals go in and out through ME parts and pattern
buffers (with Applied Mekanistics), show up in EMI recipes, and Jade shows them in a machine's current recipe.

### Gregification

Other mods' machine recipes run on GT machines and multiblocks, with GT overclocking, parallels and batch mode.
Each machine is crafted from the original mod's machine.

- **Modern Industrialization** (1.21.1 only): 14 single blocks and 10 multiblocks, including the blast furnace with
  GT coils.
- **Create**: 12 multiblocks of Create casings: millstone, crushing wheels, press, compactor, mixer (heated and
  superheated recipes run at higher tiers), encased fan (every processing kind, also from addons), deployer,
  spout, item drain, saw, mechanical crafter and a sequenced assembly line (3 to 16 slices long).
- **Mekanism**: 23 multiblocks, from the crusher and enrichment chamber to the chemical machines, the rotary
  condensentrator and the thermal evaporation plant. They take any input/output parts.
- **Productive Bees**: comb centrifuge, incubator, gene extractor, beehive (bees in cages, with upgrades and
  flowers) and breeder. The GT canner turns caged bees into spawn eggs and back, keeping their genes.
- **Hostile Neural Networks**: simulation chamber and loot fabricator, single blocks of every tier, crafted with
  field generators (simulation chamber) or a sensor and an emitter (loot fabricator).
  - Simulation chamber: a data model and a prediction matrix give the model's base drop and predictions, and the model
    learns from every craft, parallels included, when the run ends; each craft's prediction chance follows the model as
    it learns, so a craft makes on average as many predictions as the model's accuracy. With programmed circuit 1 it
    only trains the model, from any tier (Faulty too), without a prediction matrix and without outputs, at the same
    energy.
  - Loot fabricator: a ghost ingredient of the drop picks what a prediction turns into.

Gregified single blocks have batch mode, and ghost ingredients where their recipes need them.

### Survival auto build

A button on every multiblock controller builds the structure from the player's inventory: a click places up to
64 blocks, Shift-click keeps building until the structure is done. Right click opens a window for the structure's
size and block choices (for example the coil type, or the casing tier of an Assembling Matrix).

### Machine windows

Holding Shift and dragging the mouse over slots in a GTCEu machine window moves each stack to the other inventory,
the same as Shift-clicking each slot.

### Potions

- GTCEu's brewery and canner work with potions (outputs keep their potion, EMI finds the recipes).
- Splash and lingering potions are brewed in the brewery and bottled in the canner.
- Potion fluids are named after their potion, including strength and duration.
- EMI tell potion fluids apart exactly: plain, splash and lingering potions of one effect, or of other
  strengths and durations, don't mix when looking up recipes or filling them.

### Other recipes

- **Legacy part recipes**: extruder molds for rods, long rods, bolts, gears, small gears and rotors, which GTCEu
  v8 removed.
- GT wires count as other mods' wires (`c:wires/<material>`, `forge:` on 1.20.1), so their recipes take GT wires.

### GTCEu fixes

Workarounds for GTCEu bugs, removed once fixed upstream. Each branch has the ones its GTCEu needs. For example:
pattern buffers keeping the same fluid as separate entries, chanced outputs lost in very large batches,
machines going idle after repeating a recipe, a costly energy check on high-amperage energy lines, the ME stocking
bus UI not updating, the multiblock preview drawn in the wrong place in the recipe viewer, and data generation
crashing on machines with many model variants.

## Updating from older versions

- The Pattern Matrix is now the **Assembling Matrix**. Worlds saved before the rename keep their matrices, the
  patterns in their Pattern Cores and running crafting jobs.
- Smart Doubling is now **Pattern Batching**; the settings on each ME Pattern Buffer carry over.
- The fishing pond, the wireless computation hatches and the wireless optical data hatches got new IDs that match
  their names (**Industrial Fishing Pond**, **Wireless Computation Transmission / Reception Hatch**, **Wireless
  Optical Data Transmission / Reception Hatch**). Placed blocks, their contents and channel bindings are kept.
- Renamed config keys start from their defaults: `ae2.patternMatrix` → `ae2.assemblingMatrix`,
  `ae2.patternMatrixFEPerCraft` → `ae2.assemblingMatrixFEPerCraft`, `ae2.batchParts` → `ae2.patternBatchParts`,
  `machines.fishingPond` → `machines.industrialFishingPond`.

## Configuration

`config/gtmqol.yaml`. Most features have a toggle, on by default. Toggles need a restart; the options marked "live"
below apply at once. The full list, with descriptions, is in the config file itself. Without a toggle: the Magical
Assembler, the Ghost Ingredient Hatch, potion fluids, the circuit and material tags and the fixes to GTCEu.

| Group | Options |
|---|---|
| `machines` | `smartAssemblyFactory`, `easySAFRecipe`, `dimensionallyTranscendentFusionReactor`, `voidMiner` (needs `wireless.energy`), `industrialFishingPond`, `crystalGrowthChamber`, `greenhouse`, `patternCrafter`, `electricImplosionCompressor`, `advancedChipEngravingFactory`, `industrialLargeChemicalReactor`, `probableDevices`, `advancedSteamMachines` |
| `modularMachines` | `enabled` |
| `gregification` | `modernIndustrialization` (1.21.1), `mekanism`, `productiveBees`, `create`, `hostileNeuralNetworks` |
| `wireless` | `energy`, `fe`, `steam`, `computation`, `opticalData` |
| `circuits` | `universalCircuits`, `controlCircuits`, `mekanismCircuitTags` (live on `/reload`) |
| `recipes` | `miscRecipes`, `earlyGame`, `keepManualCompression`, `keepVanillaTNT`, `netherStarDust`, `useLegacyPartRecipes` |
| `steamTweaks` | `circuitSlots`, `fluidTanks`, `noOutputLimits`, `optionalLargeBoilerParts` |
| `overclocking` | `buffFusionReactor`, `enableMultiTierSkipping`, `absoluteParallelHatches` |
| `ae2` | `overclockedPatternBuffer`, `processing`, `dualHatches`, `patternBufferReturn` (live), `skipNotConsumedInputs` (live), `assemblingMatrix`, `assemblingMatrixFEPerCraft` (live, default 64), `patternBatchParts` (live, default 1), `fastCraftingPlans` (live) |
| `misc` | `feInput`, `autoBuild` (live) |
| `voidMiner` (live) | `dimensionMapping`: mine another dimension's veins (`"from=to"`, read when a Void Miner loads); `maxOperations` (default 64), `maxMultiplier` (default 4096): the most ore types and stacks per cycle |
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

- FTB Teams: shares wireless networks and team channels per team.
- AE2: the Crafting and AE2 features above, the Wireless Energy Cell, and pattern buffers split by pattern in the
  grouped recipe search.
- ExtendedAE: the sticky card works on its storage buses.
- ExtendedAE, Advanced AE, MEGA Cells, Applied Flux: recipes for their processors and materials in the ME
  Assembler and ME Circuit Slicer.
- ExtendedAE Plus: works alongside; patterns encoded for an Assembling Matrix go there before ExtendedAE Plus's own
  upload.
- Mekanism: chemicals as a recipe capability, Mekanism gregification, merged circuit tags.
- Applied Mekanistics: chemicals in ME parts and pattern buffers.
- Modern Industrialization (1.21.1): MI gregification.
- Productive Bees: Productive Bees gregification.
- Create: Create gregification.
- Hostile Neural Networks: Hostile Neural Networks gregification.
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
- The wireless energy cell texture is AE2's dense energy cell with its colors cycling.
- The Crafting Pattern textures follow Applied Energistics 2's style.
- Pattern Batching and the Assembling Matrix follow the ideas of
  [ExtendedAE Plus](https://github.com/GaLicn/ExtendedAE_Plus), and its Super Assembler Matrix.
