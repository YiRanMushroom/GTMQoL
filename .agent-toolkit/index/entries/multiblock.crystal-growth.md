+++
title = "Crystal Growth Chamber: 3x3x3, budding block in the middle gives 4 shards; recipes generated for every budding_x/x_shard"
keys = [ "amethyst", "block capability", "certus quartz", "crystalgrowthchamber", "geore" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crystal/CrystalGrowth.java"
snippet = "public static void addRecipes(RecipeOutput provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crystal/CrystalGrowthMachine.java"
snippet = "public class CrystalGrowthMachine extends WorkableElectricMultiblockMachine {"
+++
Covers ae2 flawless budding quartz, vanilla amethyst and all 28 GeOre crystals (GeOreBlockReg uses exactly these names on 1.20 and 1.21, its cluster loot is the shard). NON_Y_AXIS without extended facing because the machine reads pos.relative(front.getOpposite()). No item input bus since the block capability (old structures with one don't form). The budding block is a block-capability input: multiblock.block-provider.