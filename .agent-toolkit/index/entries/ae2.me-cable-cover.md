+++
title = "ME Cable Cover: a cover that makes a GT machine ME cabling on all six sides"
kind = "design"
keys = [ "ae2", "channels", "cover", "dense cable", "exposed sides", "grid node", "iinworldgridnodehost", "me cable cover" ]
updated = "2026-11-15 02:14:57"
checked = "2026-11-15"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/cablecover/MECableCover.java"
snippet = "public class MECableCover extends CoverBehavior implements IInWorldGridNodeHost {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/cablecover/MECableCovers.java"
snippet = "private static @Nullable IInWorldGridNodeHost host(MetaMachine machine) {"
+++
User's idea (2026-11-14), both branches, config ae2.meCableCover, ids cover me_cable / item me_cable_cover. Name with "Cable" because AE2's like things are cables (user). Recipe: GT assembler (user), Cable Anchor + ME Covered Dense Cable + steel plate + LV circuit + soldering alloy.
- Machine without an ME connection: the cover owns a managed node (DENSE_CAPACITY, in-world, all sides, no channel, no idle power), created on the first server tick after load and destroyed on unload/removal; not saved. Channels follow AE2's own channel setting (user: no limit if AE has none, multiplier if AE has one, dense cable by default), which DENSE_CAPACITY gives for free.
- AE2 finds that node through a capability provider next to GTCEu's: here AttachCapabilitiesEvent on MetaMachine (MetaMachine.getCapability falls back to attached ones; registered from MECableCovers.init in the cover registration event); on 1.21 RegisterCapabilitiesEvent.registerBlock for every MetaMachineBlock. GTCEu's own only answers for machines that are a host or have a host trait, and those never get a cover node.
- Machine with its own connection (IGridConnectedMachine): no cover node; every 10 ticks the cover looks at the machine's InWorldGridNode.isExposedOnSide, remembers the sides when not all six (the machine sets its front again when turned) and sets all; removal puts the remembered sides back. A machine open on all sides by itself is left alone.
- canAttach: a machine only, one such cover per machine, not on a block that is a node host without being IGridConnectedMachine (GTCEu's Machine Hull is ME cabling already).
- Texture: GTCEu's ME output bus overlay (resource rule), for the cover and its item.
- With it, the Assembling Matrix and ME Data Center interfaces went from all sides to front only like GTCEu's ME hatches (user, same day).