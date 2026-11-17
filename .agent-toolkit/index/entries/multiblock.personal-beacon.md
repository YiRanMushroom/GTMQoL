+++
title = "Personal Beacon: EV beacon on a titanium pyramid, gives bound players effects from cores anywhere, FE per 15 s cycle"
kind = "design"
keys = [ "beacon beam", "binding hatch", "chunk loading", "core socket", "environmental tech", "flight", "nanobot beacon", "personal beacon", "potion core", "potion core infuser" ]
updated = "2026-11-16 06:47:46"
checked = "2026-11-15"

[[items]]
name = "registration"
value = "PersonalBeacon: registration, pattern, recipes, text"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/PersonalBeacon.java"
snippet = "public final class PersonalBeacon {"

[[items]]
name = "controller"
value = "PersonalBeaconMachine: cycle, payment, grants, chunk loading"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/PersonalBeaconMachine.java"
snippet = "public class PersonalBeaconMachine extends CyclicMultiblockMachine {"

[[items]]
name = "pyramid"
value = "PersonalBeaconPyramid: the expandable pyramid"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/PersonalBeaconPyramid.java"
snippet = "public final class PersonalBeaconPyramid {"

[[items]]
name = "infuser"
value = "PotionCoreInfuserMachine"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/PotionCoreInfuserMachine.java"
snippet = "public class PotionCoreInfuserMachine extends TieredEnergyMachine implements IMuiMachine {"

[[items]]
name = "effects"
value = "BeaconEffects: Flight, Flight Speed"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/BeaconEffects.java"
snippet = "public final class BeaconEffects {"

[[items]]
name = "infuser-recipes"
value = "Recipe viewer page of the infuser: display-only recipe type potion_core_infuser, built by PotionCoreInfuserRecipes from the potion registry"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/beacon/PotionCoreInfuserRecipes.java"
snippet = "public enum PotionCoreInfuserRecipes implements GTRecipeType.ICustomRecipeLogic {"
+++
The user's design (2026-11-14), both branches, config machines.personalBeacon. Package common/beacon. Built 2026-11-14 on both branches, untried in game (status.untested).

## What players get
- Personal Beacon (multiblock, EV stage, titanium themed): built like a vanilla beacon (the user's rework, 2026-11-14): the controller alone on top of a solid pyramid of Stable Titanium Machine Casings, 1 to 7 layers; layer d below the controller is (2d+1)² (3×3, 5×5, ... 15×15).
- A layer's outer ring (what shows of it) may hold, with no count limit: Personal Beacon Core Sockets, Wireless FE Input Hatches (the only energy), Personal Beacon Binding Hatches and Personal Beacon Chunk Loaders. More layers have more of them, which is all the size does. Blocks look the same on their four sides (the user): the controller is vanilla's beacon model with a titanium base instead of obsidian (the user: almost vanilla's, see-through; no glowing texture), a noOcclusion block with light 15, the socket shows its core on six sides, white when empty and in the effect's colour otherwise, binding hatch and chunk loader have a drawn overlay on six sides; all RotationState.NONE.
- Core Socket (part): one core. n cores of one effect = level n, no cap (the user: no caps). Flight: one is all.
- Cores (items): Flight, Flight Speed, Haste, Saturation, Resistance, Health Boost, and the Potion Beacon Core (one effect, in a data component). Every effect is a MobEffect: vanilla's, plus our Flight (creative flight while it lasts) and Flight Speed (faster flying per level).
- Chunk Loader (part): while the beacon is formed, every chunk the pyramid covers stays loaded.
- Binding Hatch (part): bound to one player (Bind in its window); per effect a level from 0 to what the beacon offers, default the highest. Any number of hatches, one player each.
- Cycle of 15 s: for every bound player online (any dimension), per effect at level n V[n] EU/t (level 1 = LV; above MAX ×4 per level), Flight 1A EV; summed, × 15 s, paid in FE from the FE hatches at GTCEu's ratio. Paid: each such player gets the effects for a little more than one cycle (ambient, no particles). Not paid: nothing this cycle, so the effects run out.
- While working, a beacon beam from the controller, coloured by the effects.
- Potion Core Infuser (EV single-block machine): every tick that it holds potion fluid and 1A EV, all of it turns into time for one effect: a 250 mB bottle gives its duration × 2^(level−1), ×4 for lingering. Potions with several effects or an instant effect are refused, and so is another effect than the one stored. At 24 h a Potion Beacon Core of that effect appears in its output slot and 24 h are taken. A button clears the stored time and effect. A Potion Beacon Core never runs out.
- Recipes titanium themed: controller in the magical assembler from a Beacon, EV emitters, field generators, IV circuits; Flight core with an Elytra.

## How
- PersonalBeacon registers it all (effects through BeaconEffects, the cores, parts, controller, infuser), the pattern, recipes and text. PersonalBeaconPyramid: an ExpandablePattern whose only free bound is down (the layers); the four sideways bounds are fixed at 7 with ' ' = any around the pyramid, because the preview and auto-build offer every constraint range as its own setting. key: 's' a layer's outer ring, 'i' inside. layers(): counts the complete layers below the controller (every block a titanium casing or a machine), so a titanium floor under the pyramid is only taken for a layer when it really is one.
- PersonalBeaconMachine is a CyclicMultiblockMachine (power button; output always empty): startCycle collects grants per bound online player (one per UUID), pays all FE at once from the FE hatches (one network counted once, as the Assembling Matrix), then addEffect for EFFECT_TICKS = cycle + 3 s, ambient, no particles. offered: effect -> number of cores (Flight capped at 1, amplifier capped at 256). Beam colour = average of the effects' colours, synced; the beam shows while the render state is WORKING.
- Size changes: GTCEu re-checks a formed structure only when a block in its cache changes, and formStructure on a still-formed controller only adds parts (parts is private; ExpandablePattern never drops cache entries). So whenever formStructure runs on a formed beacon whose pattern state is VALID_UNCACHED (a full re-check, so something changed; not after a world load, where layers is still 0), it calls reform(): invalidateStructure, removeMapping, clear the caches, state UNINITIALIZED, checkAndFormStructure. The window's Check structure button calls reform() too (the user: a layer added below is not noticed by itself, that is fine with a button).
- The user (2026-11-14): any structure change drops the running cycle at once. invalidateStructure calls CyclicMultiblockMachine.cancelCycle() (WORKING to IDLE, progress 0), and reform() goes through it.
- Beam and sound: beamColor is 0 unless working, and clients draw the beam from it alone (not from the render state). setBeam marks it dirty at every cycle start, also unchanged (the user: the beam still went missing with sync on change only); it is cleared in onIdleTick (a hook of CyclicMultiblockMachine, the tick after a cycle that had no successor) and on invalidateStructure. setBeam plays BEACON_ACTIVATE / BEACON_DEACTIVATE on the change, onCycleTick BEACON_AMBIENT every 80 ticks as vanilla. The beam starts at the controller's own block so it shows through its glass.
- Chunk loading: a forced-chunk ticket per chunk within the layer count around the controller (1.21 NeoForge TicketController, 1.20 ForgeChunkManager), loadedRadius saved so the old set is released; the loading callback drops tickets of positions that hold no controller.
- Binding hatch levels: a map of explicit levels; an effect not in it gets the highest, so more cores raise it; setting the highest removes the entry.
- Branches: 1.21 Flight is NeoForge's CREATIVE_FLIGHT attribute modifier; 1.20 sets abilities.mayfly each tick and clears it on Remove/Expired (not for creative/spectator). The Potion Beacon Core's effect: 1.21 data component gtmqol:beacon_core_effect, 1.20 NBT string of the same name.
- Synced fields (beamColor, the socket's coreColor) reach the client only after syncDataHolder.markClientSyncFieldDirty(name): without it the beam never showed (the user, 2026-11-14).
- Creative tab: GTMQoLAddon.tabVariants(item, stacks) lists stacks in place of an item's plain one; used for a Potion Beacon Core of every non-instant effect (the user: for creative and EMI).
- Models: controller = our models/block/personal_beacon (vanilla's beacon.json, base texture titanium casing, render_type cutout); block properties through builder.block().properties on 1.21, blockProp on 1.20; socket = our own template models/block/personal_beacon_core_socket (differs per branch: 1.21 coplanar faces with gtceu:face_layer, 1.20 boxes 0.01/0.02 larger) whose core faces have tintindex 2, answered by the socket's tintColor from a synced @RerenderOnChanged colour; hatch and loader = createSingleOverlayTieredHullMachineModel.
- Textures: item/beacon_core (grey, tinted per effect by PersonalBeaconClient). mob_effect/flight is vanilla's elytra, flight_speed three of them stacked (the user's choice); the rest is drawn. All made by a Bun script that reads the vanilla PNGs from a client jar (not kept in the repo).