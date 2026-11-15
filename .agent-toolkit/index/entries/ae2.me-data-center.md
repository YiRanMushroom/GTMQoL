+++
title = "ME Data Center: a GT multiblock of AE2 crafting CPUs, one per job, from crafting units in CPU frames; virtual cores"
kind = "design"
keys = [ "advanced ae quantum computer", "ae2", "casing frame box", "cpu frame", "crafting cpu", "craftingcpucluster", "me data center", "tiered multiblock", "virtual crafting block entity" ]
updated = "2026-11-15 00:58:27"
checked = "2026-11-15"

[[items]]
name = "core"
value = "The never placed core block entity"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/datacenter/MEDataCenterCore.java"
snippet = "final class MEDataCenterCore extends CraftingBlockEntity"

[[items]]
name = "pool"
value = "The CPUs of one data center"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/datacenter/MEDataCenterCpus.java"
snippet = "final class MEDataCenterCpus implements INBTSerializable<CompoundTag>"

[[items]]
name = "hooks"
value = "What the two mixins call"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/datacenter/MEDataCenterHooks.java"
snippet = "public final class MEDataCenterHooks"

[[items]]
name = "box"
value = "The structure shared with the Assembling Matrix"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/CasingFrameBox.java"
snippet = "public final class CasingFrameBox"

[[items]]
name = "compat"
value = "The AE2 version check"
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/datacenter/MEDataCenterCompat.java"
snippet = "final class MEDataCenterCompat"
+++
The user's design (2026-11-14), both branches, config ae2.meDataCenter. Ids me_data_center, me_data_center_interface, <tier>_me_data_center_cpu_frame.

## What players get
- Structure: CasingFrameBox, shared with the Assembling Matrix (expandable box, quartz glass faces, edges of one tier of machine casings). One ME Data Center Interface on an edge; inside CPU frames (ULV to MAX, mixable, unrelated to the casing tier) or air. The inside renders as the End portal's stars (shaders not considered).
- A CPU frame owns tier+1 slots, one item each: any block item built on AE2's AbstractCraftingUnitBlock with storage or threads (ICraftingUnitType), so addons using AE2's unit blocks work and Advanced AE's don't. The controller's units window shows all frames' units, each kind once with its number (ItemGridEntry.count; ItemGridWidget, scrolling, no cap); a click takes up to a stack of that kind.
- Casing tier: ULV is 0, each tier above one credit; a credit is ×4 storage or ×4 co-processors. Credits start unspent; the controller has one row each for storage and co-processors with - and + (Shift: all), saved as storageCredits and coProcessorCredits, clamped at forming. Storage saturates at Long.MAX; co-processors cap at 2^30 (AE2 adds 1 to them in an int).
- Jobs: storage is one pool, a job takes plan.bytes(); every job has all the co-processors. Terminals show one CPU per job plus one with what is left (hidden at 0). Units and credits are locked while a job exists.
- No EU: AE2's power. Structure broken: jobs pause. Controller broken: jobs cancelled, what they hold goes to the network, the rest is destroyed.

## How
- A CPU is AE2's own CraftingCPUCluster, whose core block entity is MEDataCenterCore: a CraftingBlockEntity subclass that is never placed (AE2's block entity type and crafting unit state, the controller's position and level). It overrides what touches the world: node (the interface's, null while unformed: AE2 then takes the CPU as offline and the job pauses), saveChanges (marks the controller), updateSubType/breakCluster/disconnect (nothing), storage and threads (0; the data center sets the cluster's fields).
- MEDataCenterCpus is the pool: jobs' cores plus one free core; submitted() turns the free one into a job's and makes a new one; tick() drops finished and drained cores and posts GridCraftingCpuChange when the list or the grid changed. Saved in the controller ({bytes, cpu} per job); onUnload runs before the chunk is saved, so stopped jobs wait as tags.
- The interface keeps its data center after the structure breaks, so paused jobs stay in the service's list: a CPU missing from it for more than a few ticks gets its requester's link cancelled.
- AE2 internals: two append-only injections (CraftingServiceMixin.updateCPUClusters TAIL adds our CPUs and links; CraftingCPUClusterMixin.submitJob RETURN reports plan.bytes()), four access transformer lines (CraftingCPUCluster storage, accelerator, addBlockEntity, done). MEDataCenterCompat touches these by reference once and catches LinkageError: the controller then says the AE2 version is not supported. The user accepts a crash on a failed injection.
- Compatibility scan (2026-11-14, AE2 19.2.18): only CraftingBlockEntity.breakCluster and CraftingCPUCalculator touch a cluster's blocks; GTCEu, ExtendedAE, ExtendedAE Plus don't reference CraftingCPUCluster; Advanced AE also injects at updateCPUClusters TAIL for its own set and coexists.

## Rejected
GT machines inside a real AE cluster (MetaMachine is a block entity: single inheritance); a hidden dimension of real CPUs; Advanced AE's way (own cluster classes, copied CraftingCpuLogic, 18 injections); a guard layer around every AE call; splitting co-processors between jobs; EU upkeep.