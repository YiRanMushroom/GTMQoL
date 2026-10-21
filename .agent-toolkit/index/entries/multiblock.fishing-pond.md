+++
title = "Industrial Fishing Pond: vanilla fishing loot every 5 s from a rod slot, energy buffer pays m x rolls"
keys = [ "energybuffer", "fishing pond", "luck of the sea", "lure", "machines.fishingpond", "treasure mode" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/FishingPondMachine.java"
snippet = "public class FishingPondMachine extends CyclicMultiblockMachine {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/FishingPondMachine.java"
snippet = "protected void finishCycle() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/multiblock/GTMQoLMultiblocks.java"
snippet = "private static void initFishingPond() {"
+++
Structure: Large Chemical Bath widened, 5x5x4 air cavity matching vanilla's open-water check, open top; 'X' needs >=100 casings, an output bus and an INPUT_ENERGY hatch (only that ability, user's call). Needs startOffset (multiblock.start-offset). Loot needs no AT/mixin: treasure mode passes an unadded FishingHook (openWater defaults true), normal mode omits THIS_ENTITY so the treasure pool fails. Lure I/II/III: 715/429/143 EU per fish.