+++
title = "Crystal Growth Chamber: 3x3x3, budding block in the middle gives 4 shards; recipes generated for every budding_x/x_shard"
keys = [ "amethyst", "block capability", "certus quartz", "crystalgrowthchamber", "geore", "machines.crystalgrowthchamber" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crystal/CrystalGrowth.java"
snippet = "public static void addRecipes(Consumer<FinishedRecipe> provider) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crystal/CrystalGrowth.java"
snippet = "public static void initRecipeType() {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/crystal/CrystalGrowthMachine.java"
snippet = "public class CrystalGrowthMachine extends WorkableElectricMultiblockMachine {"
+++
Covers ae2 flawless budding quartz, vanilla amethyst and GeOre crystals (GeOre's 1.20 branch uses the same budding_<x>/<x>_shard names). 1.20.1: recipe type via GTRecipeTypes.register in the recipe type RegisterEvent, machine from the machine RegisterEvent. NON_Y_AXIS without extended facing because the machine reads pos.relative(front.getOpposite()). The budding block is a block-capability input: multiblock.block-provider.