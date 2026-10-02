# GTMQoL

A personal quality-of-life addon for GregTech CEu Modern (v8, Minecraft 1.20.1 Forge).

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

- The magical assembler: a tiered machine that makes this mod's items.
- Universal circuits, one per tier, which count as any circuit of that tier.
- GT circuit tags and Mekanism's circuit tags are merged (LV/MV/HV/EV ↔ basic/advanced/elite/ultimate).
  ULV circuits are merged with infused alloy.

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

### Multiblocks

- **Smart Assembly Factory**: runs assembly line recipes, with parallel hatches, perfect sub-tick
  overclocking and batch mode.
- **Dimensionally Transcendent Fusion Reactor**: runs fusion recipes of any tier, without the start energy
  requirement.

### Advanced steam multiblocks

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

### Recipe lookup performance

GTCEu's recipe search tries every combination of a machine's inputs. With many distinct buses or pattern
buffers this gets very slow in the late game. Grouped search only tries combinations that one bus, one
pattern (plus the buffer's circuit and shared inventory) or the non-distinct pool could supply, which are
the only ones GTCEu would run anyway. Technical details are in
[docs/RECIPEDB_REFACTOR.md](docs/RECIPEDB_REFACTOR.md).

## Configuration

`config/gtmqol.yaml`. All options need a restart.

| Option                                 | Default | Description                                         |
|----------------------------------------|---------|-----------------------------------------------------|
| `modularMachines.enabled`              | `true`  | Register the modular multiblocks                    |
| `overclocking.buffFusionReactor`       | `true`  | Fusion reactor buff                                 |
| `overclocking.enableMultiTierSkipping` | `true`  | Multi-tier skipping                                 |
| `integrationTests.enabled`             | `false` | Register runtime-generated example machines (dev)   |

`config/gtmqol-early.properties` holds options that are read before mods load (needs a restart):

| Option                   | Default | Description           |
|--------------------------|---------|-----------------------|
| `recipeDB.groupedSearch` | `true`  | Grouped recipe search |

## Optional dependencies

None are required.

- FTB Teams: shares wireless networks per team.
- AE2: splits pattern buffers by pattern in the grouped recipe search.
- Mekanism: merges circuit tags.

## License

Copyright (C) 2026 Yiran, Frosty. Licensed under the GNU Lesser General Public License v3.0, see
[LICENSE](LICENSE).

## Credits

- The wireless network (storage shared per player or team, data stick binding, the monitor) is inspired by
  [GTMThings](https://github.com/liansishen/GTMThings).
- The animated rainbow overlay on the wireless hatches (`overlay_wireless.png`) is taken from GTMThings'
  `overlay_energy_on_wireless`, which originally comes from GregTech: New Horizons.
- The advanced steam multiblocks and the control circuits (items and recipes) are based on
  [GT Not Leisure](https://github.com/Darknight2333/GT-Not-Leisure1) (GTNL), a GregTech: New Horizons
  addon. The control circuit textures are taken from GTNL and are licensed under LGPL-3.0.
