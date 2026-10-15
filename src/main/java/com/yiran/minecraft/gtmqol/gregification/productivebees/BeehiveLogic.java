package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.entity.bee.ConfigurableBee;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.recipe.AdvancedBeehiveRecipe;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.util.BeeAttributes;
import cy.jdkdigital.productivebees.util.BeeHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.yiran.minecraft.gtmqol.gregification.productivebees.ProductiveBeesGregification.*;

/**
 * A caged bee makes what it would bring to PB's advanced beehive, as the average over many trips: whole items always,
 * the rest as a chance. It needs its flower: a block item or item it accepts (like PB's feeder), or a bucket's worth
 * of an accepted fluid. Neither the cage, the flower nor the upgrades are used up. Upgrades work like in the hive:
 * productivity makes more, time makes it faster, the comb block upgrade (or productivity IV) gives comb blocks instead
 * of combs.
 */
enum BeehiveLogic implements GTRecipeType.ICustomRecipeLogic {

    INSTANCE;

    private static final int DURATION = 1200;
    private static final int MAX_UPGRADES = 4;
    /** Taken in this order when there are more than {@link #MAX_UPGRADES}. */
    private static final List<Supplier<? extends Item>> UPGRADES = List.of(ModItems.UPGRADE_PRODUCTIVITY_4,
            ModItems.UPGRADE_PRODUCTIVITY_3, ModItems.UPGRADE_PRODUCTIVITY_2, ModItems.UPGRADE_PRODUCTIVITY,
            ModItems.UPGRADE_TIME, ModItems.UPGRADE_COMB_BLOCK);

    @Override
    public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        Level level = level();
        if (level == null) return null;
        List<FluidStack> fluids = fluids(holder);
        for (List<ItemStack> items : inventories(holder)) {
            List<ItemStack> upgrades = new ArrayList<>();
            int left = MAX_UPGRADES;
            for (var upgrade : UPGRADES) {
                int count = Math.min(left, items.stream().filter(stack -> stack.is(upgrade.get()))
                        .mapToInt(ItemStack::getCount).sum());
                if (count <= 0) continue;
                upgrades.add(new ItemStack(upgrade.get(), count));
                left -= count;
            }
            for (ItemStack stack : items) {
                if (!BeeCage.isFilled(stack)) continue;
                GTRecipe recipe = recipe(stack, upgrades, level,
                        (bee, builder) -> addFlower(builder, bee, items, fluids));
                if (recipe != null) return recipe;
            }
        }
        return null;
    }

    /** @param flower adds the bee's flower to the recipe, false if there is none */
    private static @Nullable GTRecipe recipe(ItemStack cage, List<ItemStack> upgrades, Level level,
                                             BiPredicate<Bee, GTRecipeBuilder> flower) {
        Bee bee = BeeCage.getEntityFromStack(cage, level, true);
        if (bee == null || bee.isBaby()) return null;
        // like BeeHelper.getBeeProduce
        String beeId = beeType(bee);
        if (beeId == null) return null;
        AdvancedBeehiveRecipe hiveRecipe = hiveRecipe(beeId, level);
        if (hiveRecipe == null) return null;

        var builder = recipeType(BEEHIVE).recipeBuilder(GTMQoL.id(path(beeId)))
                .notConsumable(cage.copyWithCount(1));
        if (!flower.test(bee, builder)) return null;
        upgrades.forEach(builder::notConsumable);

        double modifier = 1 +
                config(ProductiveBeesConfig.UPGRADES.productivityMultiplier) *
                        count(upgrades, ModItems.UPGRADE_PRODUCTIVITY.get()) +
                config(ProductiveBeesConfig.UPGRADES.productivityMultiplier2) *
                        count(upgrades, ModItems.UPGRADE_PRODUCTIVITY_2.get()) +
                config(ProductiveBeesConfig.UPGRADES.productivityMultiplier3) *
                        count(upgrades, ModItems.UPGRADE_PRODUCTIVITY_3.get()) +
                config(ProductiveBeesConfig.UPGRADES.productivityMultiplier4) *
                        count(upgrades, ModItems.UPGRADE_PRODUCTIVITY_4.get());
        boolean combBlocks = count(upgrades, ModItems.UPGRADE_COMB_BLOCK.get()) +
                count(upgrades, ModItems.UPGRADE_PRODUCTIVITY_4.get()) > 0;
        // vanilla bees have no genes
        int productivity = bee instanceof ProductiveBee productive ?
                productive.getAttributeValue(BeeAttributes.PRODUCTIVITY) : 0;

        boolean any = false;
        for (var entry : hiveRecipe.getRecipeOutputs().entrySet()) {
            ItemStack stack = entry.getKey();
            int[] bounds = entry.getValue().getAsIntArray();
            int min = bounds[0];
            int max = bounds[1];
            if (combBlocks && stack.getItem() instanceof HoneycombItem) {
                ItemStack block = BeeHelper.getCombBlockFromHoneyComb(stack);
                if (!block.isEmpty()) stack = block;
            }
            double sum = 0;
            for (int count = min; count <= max; count++) {
                sum += withProductivity(count, productivity);
            }
            // PB rolls nextInt(100) <= chance
            double chance = Math.min(1, (bounds[2] + 1) / 100.0);
            double expected = modifier * chance * sum / Math.max(1, max - min + 1);
            int whole = (int) expected;
            if (whole > 0) {
                builder.outputItems(stack.copyWithCount(whole));
                any = true;
            }
            double rest = expected - whole;
            if (Math.round(rest * 10000) > 0) {
                builder.chancedOutput(stack.copyWithCount(1), chance(rest));
                any = true;
            }
        }
        if (!any) return null;

        double time = Math.max(0, 1 - config(ProductiveBeesConfig.UPGRADES.timeBonus) *
                count(upgrades, ModItems.UPGRADE_TIME.get()));
        return builder.EUt(VA[LV]).duration((int) Math.max(20, Math.round(DURATION * time))).buildRawRecipe();
    }

    private static boolean addFlower(GTRecipeBuilder builder, Bee bee, List<ItemStack> items,
                                     List<FluidStack> fluids) {
        if (!needsFlower(bee)) return true;
        for (ItemStack stack : items) {
            if (stack.getItem() instanceof BeeCage || isUpgrade(stack) || !isFlower(bee, stack)) continue;
            builder.notConsumable(stack.copyWithCount(1));
            return true;
        }
        for (FluidStack fluid : fluids) {
            if (fluid.getAmount() < FluidType.BUCKET_VOLUME || !isFlower(bee, fluid)) continue;
            builder.notConsumableFluid(FluidIngredient.of(fluid.getFluid(), FluidType.BUCKET_VOLUME));
            return true;
        }
        return false;
    }

    /** Bees whose flowers are mobs can't be given one, they go without. */
    private static boolean needsFlower(Bee bee) {
        return !(bee instanceof ProductiveBee productive && productive.getFlowerType().startsWith("entity_type"));
    }

    /**
     * PB's feeder check: a block item of a flower block, or a flower item. Configurable bees don't count their flower
     * block's item as a flower item, so the block is asked too.
     */
    private static boolean isFlower(Bee bee, ItemStack stack) {
        if (!(bee instanceof ProductiveBee productive)) return stack.is(ItemTags.FLOWERS);
        return stack.getItem() instanceof BlockItem blockItem &&
                productive.isFlowerBlock(blockItem.getBlock().defaultBlockState()) || productive.isFlowerItem(stack);
    }

    /** A flower fluid, as PB checks it in the world: its source block. */
    private static boolean isFlower(Bee bee, FluidStack fluid) {
        return bee instanceof ProductiveBee productive &&
                productive.isFlowerBlock(fluid.getFluid().defaultFluidState().createLegacyBlock());
    }

    private static boolean isUpgrade(ItemStack stack) {
        return UPGRADES.stream().anyMatch(upgrade -> stack.is(upgrade.get()));
    }

    /** For the recipe viewer: the flower from the bee's data, instead of trying every item. */
    private static boolean addViewerFlower(GTRecipeBuilder builder, Bee bee) {
        if (!needsFlower(bee)) return true;
        if (bee instanceof ConfigurableBee configurable) {
            CompoundTag data = configurable.getNBTData();
            if (data.contains("flowerTag")) {
                builder.notConsumable(ModTags.getItemTag(ResourceLocation.parse(data.getString("flowerTag"))));
            } else if (data.contains("flowerBlock")) {
                addViewerItem(builder,
                        BuiltInRegistries.BLOCK.get(ResourceLocation.parse(data.getString("flowerBlock"))).asItem());
            } else if (data.contains("flowerItem")) {
                addViewerItem(builder, BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.getString("flowerItem"))));
            } else if (data.contains("flowerFluid")) {
                String fluid = data.getString("flowerFluid");
                builder.notConsumableFluid(fluid.contains("#") ?
                        FluidIngredient.of(ModTags.getFluidTag(ResourceLocation.parse(fluid.replace("#", ""))),
                                FluidType.BUCKET_VOLUME) :
                        FluidIngredient.of(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)),
                                FluidType.BUCKET_VOLUME));
            }
            return true;
        }
        ItemStack[] flowers = Arrays.stream(Ingredient.of(ItemTags.FLOWERS).getItems())
                .filter(stack -> isFlower(bee, stack))
                .toArray(ItemStack[]::new);
        if (flowers.length > 0) builder.notConsumable(Ingredient.of(flowers));
        return true;
    }

    private static void addViewerItem(GTRecipeBuilder builder, Item item) {
        if (item != Items.AIR) builder.notConsumable(item);
    }

    /** The last matching recipe, like PB. */
    private static @Nullable AdvancedBeehiveRecipe hiveRecipe(String beeId, Level level) {
        var bee = new BeeHelper.IdentifierInventory(beeId);
        AdvancedBeehiveRecipe matched = null;
        for (AdvancedBeehiveRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get())) {
            if (recipe.matches(bee, level)) matched = recipe;
        }
        return matched;
    }

    /** The productivity gene, as PB's advanced beehive applies it to each stack. */
    private static int withProductivity(int count, int productivity) {
        if (count <= 0 || productivity <= 0) return Math.max(0, count);
        if (count == 1) return 1 + productivity;
        return count + Math.round((1f / (productivity + 2f) + (productivity + 1f) / 2f) * count);
    }

    private static int count(List<ItemStack> upgrades, Item item) {
        return upgrades.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    @Override
    public void buildRepresentativeRecipes() {
        Level level = level();
        if (level == null) return;
        GTRecipeType type = recipeType(BEEHIVE);
        // several hive recipes can be for one bee, the last one counts
        Set<String> beeIds = new HashSet<>();
        for (AdvancedBeehiveRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get())) {
            var bee = recipe.ingredient.get();
            if (bee == null || !beeIds.add(bee.getBeeType().toString())) continue;
            ItemStack cage = cage(bee, level);
            if (cage == null) continue;
            GTRecipe gtRecipe = recipe(cage, List.of(), level, (b, builder) -> addViewerFlower(builder, b));
            if (gtRecipe != null) addRepresentative(type, gtRecipe);
        }
    }
}
