package com.yiran.minecraft.gtmqol.gregification.productivebees;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.ForeignRecipeConverter;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import cy.jdkdigital.productivebees.ProductiveBeesConfig;
import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient;
import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredientFactory;
import cy.jdkdigital.productivebees.common.item.BeeCage;
import cy.jdkdigital.productivebees.common.recipe.CentrifugeRecipe;
import cy.jdkdigital.productivebees.init.ModItems;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivebees.util.BeeHelper;
import cy.jdkdigital.productivelib.common.recipe.TagOutputRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.*;

/**
 * Productive Bees' machines as tiered GT single blocks (and so, through {@code ModularMachines}, modular
 * multiblocks): the comb centrifuge runs PB's centrifuge recipes, the incubator, gene extractor, beehive and
 * breeder are custom recipe logic, since what they make depends on the bee in the cage.
 *
 * <p>Centrifuge recipes are converted while the recipe manager loads, before tags are bound: everything here keeps
 * PB's ingredients as they are instead of resolving them to items.</p>
 */
public final class ProductiveBeesGregification {

    private static final String PB = "productivebees";
    static final String CENTRIFUGE = "pb_centrifuge";
    static final String INCUBATOR = "pb_incubator";
    static final String GENE_EXTRACTOR = "pb_gene_extractor";
    static final String BEEHIVE = "pb_beehive";
    static final String BREEDER = "pb_breeder";

    private ProductiveBeesGregification() {}

    /** Lets gtceu's canner move bees between cages and spawn eggs. */
    public static void init(IEventBus modBus) {
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", BeehiveUI.INFO, "Works like PB's advanced beehive");
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(
                () -> GTRecipeTypes.CANNER_RECIPES.get().getCustomRecipeLogicRunners().add(CannerBeeLogic.INSTANCE)));
    }

    public static List<ForeignMachineType> types() {
        return List.of(
                ForeignMachineType.singleBlock(CENTRIFUGE, "PB Comb Centrifuge", ModRecipeTypes.CENTRIFUGE_TYPE,
                        ProductiveBeesGregification::convertCentrifuge, 1, 4, 0, 1, GTSoundEntries.CENTRIFUGE,
                        builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT), pb("powered_centrifuge"),
                        tier -> gt(tier, "centrifuge"), GTCEu.id("block/machines/centrifuge")),
                ForeignMachineType.singleBlock(INCUBATOR, "PB Industrial Incubator", null, null, 2, 1, 0, 0,
                                GTSoundEntries.CHEMICAL,
                                builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_CRYSTALLIZATION),
                                pb("incubator"), tier -> gt(tier, "autoclave"), GTCEu.id("block/machines/autoclave"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(IncubatorLogic.INSTANCE)),
                ForeignMachineType.singleBlock(GENE_EXTRACTOR, "PB Industrial Gene Extractor", null, null, 1, 7, 0, 0,
                                GTSoundEntries.COMPRESSOR,
                                builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT),
                                pb("gene_indexer"), tier -> gt(tier, "extractor"), GTCEu.id("block/machines/extractor"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(GeneExtractorLogic.INSTANCE)),
                ForeignMachineType.singleBlock(BEEHIVE, "PB Industrial Beehive", null, null, 6, 6, 1, 0,
                                GTSoundEntries.MIXER, BeehiveUI::layout, pb("advanced_oak_beehive"),
                                tier -> gt(tier, "brewery"), GTCEu.id("block/machines/brewery"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(BeehiveLogic.INSTANCE))
                        .withTooltip(BeehiveUI.INFO),
                ForeignMachineType.singleBlock(BREEDER, "PB Industrial Breeder", null, null,
                                BreederUI.CAGES + BreederUI.ITEMS, 16, 0, 0, GTSoundEntries.CHEMICAL,
                                BreederUI::layout, pb("breeding_chamber"), tier -> gt(tier, "chemical_reactor"),
                                GTCEu.id("block/machines/chemical_reactor"))
                        .withRecipeType(builder -> builder.addCustomRecipeLogic(BreederLogic.INSTANCE)));
    }

    private static ResourceLocation pb(String path) {
        return ResourceLocation.fromNamespaceAndPath(PB, path);
    }

    private static ResourceLocation gt(int tier, String machine) {
        return GTCEu.id(VN[tier].toLowerCase(Locale.ROOT) + "_" + machine);
    }

    // Centrifuge

    /**
     * Like PB's powered centrifuge, which takes a third of the time; chances include the bonus PB's centrifuges
     * give without a stability upgrade. Comb blocks get a second recipe like PB's heated centrifuge: four combs'
     * worth at once, without the wax.
     */
    private static void convertCentrifuge(RecipeHolder<?> holder, ForeignRecipeConverter.Output output) {
        if (!(holder.value() instanceof CentrifugeRecipe recipe)) return;
        int duration = Math.max(1, config(ProductiveBeesConfig.GENERAL.centrifugeProcessingTime) / 3);
        output.add(builder -> {
            builder.inputItems(recipe.ingredient);
            if (!addCentrifugeOutputs(builder, recipe, 1)) return false;
            builder.EUt(VA[LV]).duration(duration);
            return true;
        });
        ItemStack combBlock = combBlock(recipe.ingredient);
        if (combBlock.isEmpty()) return;
        output.add(builder -> {
            builder.inputItems(combBlock);
            if (!addCentrifugeOutputs(builder, recipe, 4)) return false;
            builder.EUt(VA[LV]).duration(duration * 4);
            return true;
        });
    }

    /** @return false if there's no output at all */
    private static boolean addCentrifugeOutputs(GTRecipeBuilder builder, CentrifugeRecipe recipe, int times) {
        double bonus = config(ProductiveBeesConfig.UPGRADES.stabilityChanceIncrease);
        boolean any = false;
        for (TagOutputRecipe.ChancedOutput output : recipe.itemOutput) {
            if (times > 1 && isWax(output.ingredient())) continue;
            int min = output.min() * times;
            int max = output.max() * times;
            if (max <= 0) continue;
            Ingredient ingredient = min == max ? output.ingredient() :
                    IntProviderIngredient.of(output.ingredient(), UniformInt.of(min, max)).toVanilla();
            Gregification.chancedOutput(builder, new SizedIngredient(ingredient, max), output.chance() + bonus);
            any = true;
        }
        SizedFluidIngredient fluid = recipe.fluidOutput;
        if (fluid != null && fluid.amount() > 0) {
            builder.outputFluids(new SizedFluidIngredient(fluid.ingredient(), fluid.amount() * times));
            any = true;
        }
        return any;
    }

    /**
     * The comb block of a comb, if the ingredient is one: PB's combs are items, or component ingredients for the
     * configurable ones. Tag ingredients are left out, their items aren't known yet.
     */
    private static ItemStack combBlock(Ingredient comb) {
        if (!comb.isCustom() && !Arrays.stream(comb.getValues()).allMatch(v -> v instanceof Ingredient.ItemValue)) {
            return ItemStack.EMPTY;
        }
        ItemStack[] items = comb.getItems();
        if (items.length != 1) return ItemStack.EMPTY;
        return BeeHelper.getCombBlockFromHoneyComb(items[0]);
    }

    /** PB's centrifuge recipes all give wax as the item, not the tag. */
    private static boolean isWax(Ingredient ingredient) {
        return !ingredient.isCustom() && Arrays.stream(ingredient.getValues())
                .anyMatch(v -> v instanceof Ingredient.ItemValue item && item.item().is(ModItems.WAX.get()));
    }

    // Shared by the custom logic

    /** PB's server config, which isn't loaded yet when recipes first load. */
    static <T> T config(ModConfigSpec.ConfigValue<T> value) {
        return ProductiveBeesConfig.SERVER_CONFIG.isLoaded() ? value.get() : value.getDefault();
    }

    static GTRecipeType recipeType(String name) {
        return (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(GTMQoL.id(name));
    }

    /** For a recipe the recipe viewer shows: the leading '/' marks it synthetic. */
    static void addRepresentative(GTRecipeType type, GTRecipe recipe) {
        recipe.setId(recipe.getId().withPrefix("/"));
        type.addToMainCategory(recipe);
    }

    /** Recipe id path for a bee type, e.g. {@code productivebees/iron}; the builder adds the type's path. */
    static String path(String beeType) {
        return beeType.replace(':', '/');
    }

    /** The server's level, or the client's one when the recipe viewer loads on a remote server. */
    static @Nullable Level level() {
        var server = GTCEu.getMinecraftServer();
        return server != null ? server.overworld() : GregificationClient.level();
    }

    /** Input items of each distinct bus on its own, then of all the others together. */
    static List<List<ItemStack>> inventories(IRecipeCapabilityHolder holder) {
        List<List<ItemStack>> inventories = new ArrayList<>();
        List<ItemStack> shared = new ArrayList<>();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            List<ItemStack> items = handlerList.isDistinct() ? new ArrayList<>() : shared;
            for (var handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof ItemStack stack && !stack.isEmpty()) items.add(stack);
                }
            }
            if (handlerList.isDistinct() && !items.isEmpty()) inventories.add(items);
        }
        if (!shared.isEmpty()) inventories.add(shared);
        return inventories;
    }

    /** Input fluids of all hatches together. */
    static List<FluidStack> fluids(IRecipeCapabilityHolder holder) {
        List<FluidStack> fluids = new ArrayList<>();
        for (var handlerList : holder.getCapabilitiesForIO(IO.IN)) {
            for (var handler : handlerList.getCapability(FluidRecipeCapability.CAP)) {
                for (Object content : handler.getContents()) {
                    if (content instanceof FluidStack stack && !stack.isEmpty()) fluids.add(stack);
                }
            }
        }
        return fluids;
    }

    /**
     * {@link #level()}, with every bee's entity made and cached ({@link BeeIngredient#getCachedEntity}) on the level's
     * own thread, where entities may only be made: recipe viewers build their recipes on a worker thread (EMI's crash
     * otherwise: "Accessing LegacyRandomSource from multiple threads"). That thread waits for it.
     */
    static @Nullable Level levelWithBees() {
        Level level = level();
        if (level == null) return null;
        Runnable makeBees = () -> {
            for (var bee : bees()) bee.getCachedEntity(level);
        };
        BlockableEventLoop<?> owner = level instanceof ServerLevel server ? server.getServer() :
                GregificationClient.executor();
        if (owner.isSameThread()) makeBees.run();
        else owner.submit(makeBees).join();
        return level;
    }

    /** Every bee, by id. */
    static List<BeeIngredient> bees() {
        return BeeIngredientFactory.getOrCreateList(true).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
    }

    /** A cage holding the bee, for recipes the recipe viewer shows. */
    static @Nullable ItemStack cage(BeeIngredient ingredient, Level level) {
        if (!(ingredient.getCachedEntity(level) instanceof Bee bee)) return null;
        return cage(bee);
    }

    static ItemStack cage(Bee bee) {
        ItemStack cage = new ItemStack(ModItems.BEE_CAGE.get());
        BeeCage.captureEntity(bee, cage);
        return cage;
    }
}
