package com.yiran.minecraft.gtmqol.common.multiblock;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.NoopVeinGenerator;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import it.unimi.dsi.fastutil.objects.Object2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

/**
 * What the void miner produces in a dimension: the GTCEu ore veins of that dimension, averaged by vein weight.
 */
public final class VoidMinerOres {

    public record OreChance(Item item, double chance) {

        public static OreChance read(FriendlyByteBuf buf) {
            return new OreChance(buf.readById(BuiltInRegistries.ITEM), buf.readDouble());
        }

        public static void write(FriendlyByteBuf buf, OreChance ore) {
            buf.writeId(BuiltInRegistries.ITEM, ore.item);
            buf.writeDouble(ore.chance);
        }
    }

    private VoidMinerOres() {}

    /**
     * Sorted by chance, highest first; chances add up to 1. Empty if the dimension has no veins.
     */
    public static List<OreChance> compute(ServerLevel level) {
        ResourceKey<Level> dimension = mapDimension(level.dimension());
        TagPrefix prefix = orePrefix(level.getServer(), dimension);

        // Each vein contributes its weight, split between its materials by their chance within the vein.
        Object2DoubleMap<Item> weights = new Object2DoubleLinkedOpenHashMap<>();
        for (GTOreDefinition vein : GTRegistries.ORE_VEINS) {
            if (vein.weight() <= 0 || vein.veinGenerator() == null ||
                    vein.veinGenerator() instanceof NoopVeinGenerator ||
                    !vein.dimensionFilter().contains(dimension)) {
                continue;
            }
            List<ObjectIntPair<Material>> entries = vein.veinGenerator().getValidMaterialsChances();
            int total = entries.stream().mapToInt(ObjectIntPair::rightInt).sum();
            if (total <= 0) continue;
            for (ObjectIntPair<Material> entry : entries) {
                Item item = ChemicalHelper.getItem(prefix, entry.left());
                if (item == null || item == Items.AIR) continue;
                weights.mergeDouble(item, (double) vein.weight() * entry.rightInt() / total, Double::sum);
            }
        }

        double sum = weights.values().doubleStream().sum();
        List<OreChance> result = new ArrayList<>();
        if (sum <= 0) return result;
        weights.forEach((item, weight) -> result.add(new OreChance(item, weight / sum)));
        result.sort(Comparator.comparingDouble(OreChance::chance).reversed());
        return result;
    }

    /**
     * Applies {@code voidMiner.dimensionMapping} from the config.
     */
    private static ResourceKey<Level> mapDimension(ResourceKey<Level> dimension) {
        for (String entry : GTMQoLConfig.get().voidMiner.dimensionMapping) {
            int i = entry.indexOf('=');
            ResourceLocation from = i > 0 ? ResourceLocation.tryParse(entry.substring(0, i).trim()) : null;
            ResourceLocation to = i > 0 ? ResourceLocation.tryParse(entry.substring(i + 1).trim()) : null;
            if (from == null || to == null) {
                GTMQoL.LOGGER.warn("Invalid void miner dimension mapping '{}', expected 'from=to'", entry);
                continue;
            }
            if (from.equals(dimension.location())) {
                return ResourceKey.create(Registries.DIMENSION, to);
            }
        }
        return dimension;
    }

    /**
     * Deepslate, netherrack and end stone ores for the vanilla dimensions. Elsewhere the ore whose stone is the
     * dimension's default block (from its noise settings), or deepslate if there is none.
     */
    private static TagPrefix orePrefix(MinecraftServer server, ResourceKey<Level> dimension) {
        if (dimension == Level.OVERWORLD) return oreDeepslate;
        if (dimension == Level.NETHER) return oreNetherrack;
        if (dimension == Level.END) return oreEndstone;
        ServerLevel level = server.getLevel(dimension);
        if (level != null && level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator noise) {
            Block stone = noise.generatorSettings().value().defaultBlock().getBlock();
            for (var entry : TagPrefix.ORES.entrySet()) {
                if (entry.getValue().stoneType().get().is(stone)) return entry.getKey();
            }
        }
        return oreDeepslate;
    }
}
