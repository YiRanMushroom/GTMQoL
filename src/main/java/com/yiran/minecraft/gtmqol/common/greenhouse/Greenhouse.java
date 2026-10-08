package com.yiran.minecraft.gtmqol.common.greenhouse;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.integration.mysticalagriculture.MAGreenhouseRecipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.fml.ModList;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Water;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.*;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/**
 * Greenhouse: a single block in every electric tier, and the Industrial Greenhouse, an IV 5 × 5 × 5 glass multiblock
 * with a dirt block in the middle of its floor, 16 times the outputs, parallel hatches and perfect overclocks. One plain recipe per seed or sapling, which is a
 * non-consumed input (so every recipe has distinct inputs, see {@code CrystalGrowth}), plus water. Vanilla and
 * GTCEu plants are listed by hand, Mystical Agriculture crops come from its crop registry, and other mods' saplings
 * and crops are guessed from their names.
 */
public final class Greenhouse {

    private static final String KEY = "gtmqol.machine.greenhouse.";
    private static final String INDUSTRIAL_KEY = "gtmqol.multiblock.industrial_greenhouse.";

    public static final int CROP_DURATION = 200;
    public static final int TREE_DURATION = 400;

    public static GTRecipeTypeEntry RECIPE_TYPE;
    @SuppressWarnings("unchecked")
    public static MachineEntry<MachineDefinition>[] MACHINES = new MachineEntry[TIER_COUNT];
    public static MachineEntry<MultiblockMachineDefinition> INDUSTRIAL;

    /** Seeds and saplings that already have a recipe, so the name guesses don't add a conflicting second one. */
    private static final Set<Item> PLANTED = new HashSet<>();

    /** Multiplies every output of the Industrial Greenhouse. */
    public static final int INDUSTRIAL_OUTPUT_MULTIPLIER = 16;
    private static final RecipeModifier INDUSTRIAL_OUTPUT = (machine, recipe) -> ModifierFunction.builder()
            .outputModifier(ContentModifier.multiplier(INDUSTRIAL_OUTPUT_MULTIPLIER))
            .build();

    private Greenhouse() {}

    public static void init() {
        var registrate = GTMQoLAddon.registrate();
        RECIPE_TYPE = registrate.recipeType("greenhouse", GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(1, 4, 1, 0)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
                .lang("Greenhouse")
                .register();

        for (int tier : ELECTRIC_TIERS) {
            MACHINES[tier] = GTMQoLAddon.machine(VN[tier].toLowerCase(Locale.ROOT) + "_greenhouse",
                    info -> new SimpleTieredMachine(info, tier))
                    .tier(tier)
                    .langValue("%s Greenhouse %s".formatted(VLVH[tier], VLVT[tier]))
                    .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(RECIPE_TYPE)
                    .recipeModifier(OC_NON_PERFECT)
                    .workableTieredHullModel(GTCEu.id("block/machines/fermenter"))
                    .tooltips(Component.translatable(KEY + "tooltip"))
                    .tooltips(workableTiered(tier, V[tier], V[tier] * 64, RECIPE_TYPE,
                            defaultTankSizeFunction.applyAsInt(tier), true))
                    .tooltips(explosion())
                    .register();
        }

        INDUSTRIAL = GTMQoLAddon.multiblock("industrial_greenhouse", WorkableElectricMultiblockMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(RECIPE_TYPE)
                .recipeModifiers(PARALLEL_HATCH, INDUSTRIAL_OUTPUT, OC_PERFECT_SUBTICK)
                .tooltips(Component.translatable(INDUSTRIAL_KEY + "tooltip.0"),
                        Component.translatable(INDUSTRIAL_KEY + "tooltip.1", INDUSTRIAL_OUTPUT_MULTIPLIER),
                        Component.translatable(INDUSTRIAL_KEY + "tooltip.2"))
                .appearanceBlock(CASING_TUNGSTENSTEEL_ROBUST)
                // slices go back to front, strings bottom to top: a casing floor, glass walls on casing pillars and
                // a glass roof. D is the middle of the floor's inside, the rest of the inside is free.
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXXXX", "XGGGX", "XGGGX", "XGGGX", "XXXXX")
                        .slice("XXXXX", "G###G", "G###G", "G###G", "XGGGX")
                        .slice("XXXXX", "G#D#G", "G###G", "G###G", "XGGGX")
                        .slice("XXXXX", "G###G", "G###G", "G###G", "XGGGX")
                        .slice("XXXXX", "XGSGX", "XGGGX", "XGGGX", "XXXXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(CASING_TUNGSTENSTEEL_ROBUST.get())
                                .and(autoAbilities(definition.getRecipeTypes()))
                                .and(autoAbilities(true, false, true)))
                        .where('G', blocks(CASING_LAMINATED_GLASS.get())
                                .or(blocks(CASING_TUNGSTENSTEEL_ROBUST.get())))
                        .where('D', blockTag(BlockTags.DIRT))
                        .where('#', any())
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                        GTCEu.id("block/multiblock/large_chemical_reactor"))
                .langValue("Industrial Greenhouse")
                .register();

        registrate.addRawLang(KEY + "tooltip", "Grows the seed or sapling in its input slot, which is not consumed.");
        registrate.addRawLang(INDUSTRIAL_KEY + "tooltip.0",
                "Grows the seed or sapling in its input bus, which is not consumed.");
        registrate.addRawLang(INDUSTRIAL_KEY + "tooltip.1", "Outputs %sx as much.");
        registrate.addRawLang(INDUSTRIAL_KEY + "tooltip.2", "Needs dirt, grass or the like in the middle of its floor.");
    }

    public static void addRecipes(RecipeOutput provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, MACHINES, "WPW", "GMG", "WCW",
                'M', HULL, 'P', PUMP, 'C', CIRCUIT, 'W', CABLE, 'G', GLASS);
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTMQoL.id("industrial_greenhouse"),
                INDUSTRIAL.asStack(), "PCP", "GMG", "PCP",
                'G', CASING_LAMINATED_GLASS.asItem(),
                'C', CustomTags.LuV_CIRCUITS,
                'P', GTItems.ELECTRIC_PUMP_IV.asStack(),
                'M', MACHINES[IV].asStack());

        PLANTED.clear();
        addTrees(provider);
        addCrops(provider);
        if (ModList.get().isLoaded("mysticalagriculture")) MAGreenhouseRecipes.addRecipes(provider);
        // last, so they skip everything above
        addGuessedPlants(provider);
    }

    private static void addTrees(RecipeOutput provider) {
        tree(Items.OAK_SAPLING, Items.OAK_LOG, Items.OAK_LEAVES).chancedOutput(Items.APPLE, 2, "1/2").save(provider);
        tree(Items.SPRUCE_SAPLING, Items.SPRUCE_LOG, Items.SPRUCE_LEAVES).save(provider);
        tree(Items.BIRCH_SAPLING, Items.BIRCH_LOG, Items.BIRCH_LEAVES).save(provider);
        tree(Items.JUNGLE_SAPLING, Items.JUNGLE_LOG, Items.JUNGLE_LEAVES).save(provider);
        tree(Items.ACACIA_SAPLING, Items.ACACIA_LOG, Items.ACACIA_LEAVES).save(provider);
        tree(Items.DARK_OAK_SAPLING, Items.DARK_OAK_LOG, Items.DARK_OAK_LEAVES)
                .chancedOutput(Items.APPLE, 2, "1/2").save(provider);
        tree(Items.CHERRY_SAPLING, Items.CHERRY_LOG, Items.CHERRY_LEAVES).save(provider);
        tree(Items.MANGROVE_PROPAGULE, Items.MANGROVE_LOG, Items.MANGROVE_LEAVES)
                .outputItems(Items.MANGROVE_ROOTS, 4).save(provider);
        // azaleas grow into oak trees with azalea leaves
        tree(Items.AZALEA, Items.OAK_LOG, Items.AZALEA_LEAVES).save(provider);
        tree(Items.FLOWERING_AZALEA, Items.OAK_LOG, Items.FLOWERING_AZALEA_LEAVES).save(provider);
        tree(Items.CRIMSON_FUNGUS, Items.CRIMSON_STEM, Items.NETHER_WART_BLOCK)
                .outputItems(Items.SHROOMLIGHT, 2).save(provider);
        tree(Items.WARPED_FUNGUS, Items.WARPED_STEM, Items.WARPED_WART_BLOCK)
                .outputItems(Items.SHROOMLIGHT, 2).save(provider);

        tree(RUBBER_SAPLING.asItem(), RUBBER_LOG.asItem(), RUBBER_LEAVES.asItem())
                .outputItems(GTItems.STICKY_RESIN.asItem(), 8).save(provider);
    }

    private static void addCrops(RecipeOutput provider) {
        crop(Items.WHEAT_SEEDS).outputItems(Items.WHEAT, 8).outputItems(Items.WHEAT_SEEDS, 4).save(provider);
        crop(Items.BEETROOT_SEEDS).outputItems(Items.BEETROOT, 8).outputItems(Items.BEETROOT_SEEDS, 4).save(provider);
        crop(Items.CARROT).outputItems(Items.CARROT, 12).save(provider);
        crop(Items.POTATO).outputItems(Items.POTATO, 12).chancedOutput(Items.POISONOUS_POTATO, "1/4").save(provider);
        crop(Items.MELON_SEEDS).outputItems(Items.MELON_SLICE, 24).save(provider);
        crop(Items.PUMPKIN_SEEDS).outputItems(Items.PUMPKIN, 4).save(provider);
        crop(Items.TORCHFLOWER_SEEDS).outputItems(Items.TORCHFLOWER, 2).save(provider);
        crop(Items.PITCHER_POD).outputItems(Items.PITCHER_PLANT, 2).save(provider);
        crop(Items.SUGAR_CANE).outputItems(Items.SUGAR_CANE, 12).save(provider);
        crop(Items.CACTUS).outputItems(Items.CACTUS, 8).save(provider);
        crop(Items.BAMBOO).outputItems(Items.BAMBOO, 16).save(provider);
        crop(Items.KELP).outputItems(Items.KELP, 12).save(provider);
        crop(Items.COCOA_BEANS).outputItems(Items.COCOA_BEANS, 12).save(provider);
        crop(Items.SWEET_BERRIES).outputItems(Items.SWEET_BERRIES, 12).save(provider);
        crop(Items.GLOW_BERRIES).outputItems(Items.GLOW_BERRIES, 12).save(provider);
        crop(Items.NETHER_WART).outputItems(Items.NETHER_WART, 8).save(provider);
        crop(Items.CHORUS_FLOWER).outputItems(Items.CHORUS_FRUIT, 8).save(provider);
        crop(Items.BROWN_MUSHROOM).outputItems(Items.BROWN_MUSHROOM, 8).save(provider);
        crop(Items.RED_MUSHROOM).outputItems(Items.RED_MUSHROOM, 8).save(provider);
    }

    /**
     * Other mods' plants, by block class and item name:
     * <ul>
     * <li>{@code <ns>:<x>_sapling} placing a {@link SaplingBlock} gives {@code <ns>:<x>_log} and {@code <x>_leaves};
     * <li>an item placing a {@link CropBlock} gives {@code <ns>:<x>} if it is {@code <x>_seeds}, else more of
     * itself (like carrots).
     * </ul>
     */
    private static void addGuessedPlants(RecipeOutput provider) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (PLANTED.contains(item) || !(item instanceof BlockItem blockItem)) continue;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id.getNamespace().equals("minecraft")) continue;
            String path = id.getPath();

            if (blockItem.getBlock() instanceof SaplingBlock && path.endsWith("_sapling")) {
                String name = path.substring(0, path.length() - "_sapling".length());
                var log = item(id.getNamespace(), name + "_log");
                if (log.isEmpty()) continue;
                var builder = tree(item, log.get(), null);
                item(id.getNamespace(), name + "_leaves").ifPresent(leaves -> builder.outputItems(leaves, 8));
                builder.save(provider);
            } else if (blockItem.getBlock() instanceof CropBlock) {
                if (path.endsWith("_seeds")) {
                    String name = path.substring(0, path.length() - "_seeds".length());
                    var produce = item(id.getNamespace(), name);
                    if (produce.isEmpty()) continue;
                    crop(item).outputItems(produce.get(), 8).outputItems(item, 4).save(provider);
                } else {
                    crop(item).outputItems(item, 12).save(provider);
                }
            }
        }
    }

    private static Optional<Item> item(String namespace, String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    /** 16 logs, 8 leaves (if any) and 2 saplings back. */
    private static GTRecipeBuilder tree(Item sapling, Item log, Item leaves) {
        var builder = plant(sapling, TREE_DURATION).outputItems(log, 16);
        if (leaves != null) builder.outputItems(leaves, 8);
        return builder.outputItems(sapling, 2);
    }

    private static GTRecipeBuilder crop(Item seed) {
        return plant(seed, CROP_DURATION);
    }

    /** A recipe that grows {@code seed} (not consumed) with water; add the outputs and save it. */
    public static GTRecipeBuilder plant(Item seed, int duration) {
        PLANTED.add(seed);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(seed);
        return RECIPE_TYPE.recipeBuilder(GTMQoL.id(id.getNamespace() + "_" + id.getPath()))
                .notConsumable(seed)
                .inputFluids(Water, 1000)
                .duration(duration)
                .EUt(VA[LV]);
    }
}
