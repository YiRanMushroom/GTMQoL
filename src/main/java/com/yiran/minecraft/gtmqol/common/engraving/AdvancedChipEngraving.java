package com.yiran.minecraft.gtmqol.common.engraving;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredientRecipeCapability;
import com.yiran.minecraft.gtmqol.common.ghost.ItemGhostIngredient;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plateDouble;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_LASER_SAFE_ENGRAVING;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;

/**
 * Advanced Chip Engraving Factory: every laser engraver recipe gets a copy in its own recipe type without the lens
 * (and any other input that isn't consumed) and without its conditions (cleanroom); a copy engraving a wafer takes
 * four times as long for four times the output, the others are as long and give as much as the laser engraver.
 * Without the lens, recipes that differed only in it take the same input, so each copy takes its product as a ghost
 * ingredient: the player marks what to make (or the pattern buffer's Mark Pattern Outputs does).
 * <p>
 * The copies come from the laser engraver type's {@code onRecipeBuild}, as for the Electric Implosion Compressor
 * ({@code ElectricImplosion}). Recipes added by KubeJS don't go through it.
 */
public final class AdvancedChipEngraving {

    public static GTRecipeType RECIPE_TYPE;
    public static MultiblockMachineDefinition MACHINE;

    private static final String ID = "advanced_chip_engraving_factory";
    private static final String NAME = "Advanced Chip Engraving Factory";
    private static final String TOOLTIP_KEY = "gtmqol.multiblock." + ID + ".tooltip.";
    /** Duration and outputs of a copy whose input is a wafer, against the laser engraver recipe; others keep both. */
    private static final int WAFER_MULTIPLIER = 4;

    private AdvancedChipEngraving() {}

    /** The automation line only where the Overclocked ME Pattern Buffer exists. */
    private static Component[] tooltips() {
        List<Component> lines = new ArrayList<>(List.of(
                Component.translatable("gtceu.multiblock.parallelizable.tooltip"),
                Component.translatable(TOOLTIP_KEY + "0"),
                Component.translatable(TOOLTIP_KEY + "1")));
        if (GTCEu.Mods.isAE2Loaded() && GTMQoLConfig.get().ae2.overclockedPatternBuffer) {
            lines.add(Component.translatable(TOOLTIP_KEY + "2"));
        }
        return lines.toArray(Component[]::new);
    }

    /** From GTCEu's recipe type {@code RegisterEvent}. */
    public static void initRecipeType() {
        RECIPE_TYPE = GTRecipeTypes.register(GTMQoL.id(ID), GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(1, 1, 0, 0)
                .setMaxSize(IO.IN, GhostIngredientRecipeCapability.CAP, 1)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
                .setSound(GTSoundEntries.ELECTROLYZER);
        GTMQoLAddon.registrate().addRawLang(RECIPE_TYPE.registryName.toLanguageKey(), NAME);

        // There is no getter for the prototype; recipeBuilder(id) copies it, onSave included.
        BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> previous =
                GTRecipeTypes.LASER_ENGRAVER_RECIPES.recipeBuilder(GTMQoL.id("dummy")).onSave;
        GTRecipeTypes.LASER_ENGRAVER_RECIPES.onRecipeBuild((builder, provider) -> {
            if (previous != null) previous.accept(builder, provider);
            addLenslessCopy(builder, provider);
        });
    }

    /** From GTCEu's machine {@code RegisterEvent}. */
    public static void initMachine() {
        // A hall of Laser-Safe Engraving Casings, like GTCEu's Large Engraving Laser: slices run left to right, the
        // front wall has a Tempered Glass window at each of the four laser heads (Tungstensteel Pipe Casings) that
        // hang over a line of Grate Casings. The controller is in the middle of the front, at the bottom.
        MACHINE = GTMQoLAddon.multiblock(ID, WorkableElectricMultiblockMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(RECIPE_TYPE)
                .recipeModifiers(DEFAULT_ENVIRONMENT_REQUIREMENT, PARALLEL_HATCH, OC_NON_PERFECT_SUBTICK, BATCH_MODE)
                .tooltips(tooltips())
                .conditionalTooltip(GTMachineUtils.defaultEnvironmentRequirement(),
                        ConfigHolder.INSTANCE.gameplay.environmentalHazards)
                .appearanceBlock(CASING_LASER_SAFE_ENGRAVING)
                .pattern(definition -> {
                    String[] end = { "XXXXXXX", "XXXXXXX", "XXGGGXX", "XXGGGXX", "XXGGGXX", "XXXXXXX", "XXXXXXX" };
                    String[] rib = { "XXXXXXX", "XAKKKAX", "XAAAAAX", "XAAAAAX", "XAAAAAX", "XAAAAAX", "XXXXXXX" };
                    String[] laser = { "XXXXXXX", "GAKKKAX", "GAAAAAX", "GAAAAAX", "GAACAAX", "GACCCAX", "XXXXXXX" };
                    String[] middle = { "SXXXXXX", "XAKKKAX", "XAAAAAX", "XAAAAAX", "XAAAAAX", "XAAAAAX", "XXXXXXX" };
                    return MultiblockPatternBuilder.start(RIGHT, UP, BACK)
                            .slice(end).slice(rib).slice(laser).slice(rib).slice(laser)
                            .slice(middle)
                            .slice(laser).slice(rib).slice(laser).slice(rib).slice(end)
                            .where('S', controller(definition))
                            .where('X', blocks(CASING_LASER_SAFE_ENGRAVING.get())
                                    .and(autoAbilities(definition.getRecipeTypes()))
                                    .and(autoAbilities(true, false, true)))
                            .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                            .where('C', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where('K', blocks(CASING_GRATE.get()))
                            .where('A', air())
                            .build();
                })
                .workableCasingModel(GTCEu.id("block/casings/gcym/laser_safe_engraving_casing"),
                        GTCEu.id("block/multiblock/gcym/large_engraving_laser"))
                .langValue(NAME)
                .register();
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + "0",
                "Runs Laser Engraver recipes without the lens or a cleanroom. Wafers take 4× the time for 4× " +
                        "the output.");
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + "1", "Mark the product in a Ghost Ingredient Hatch.");
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + "2",
                "To automate, turn on Mark Pattern Outputs on an Overclocked ME Pattern Buffer.");
        // Untested: the Advanced Chip Engraving Factory forms (structure preview, auto build) and runs a lensless
        // copy only with its product marked: e.g. a Silicon Wafer and a marked Integrated Logic Circuit Wafer make
        // 4 of them in 4x the laser engraver's time, a non-wafer input (an Engraved Crystal Chip) gives what the laser
        // engraver gives in its time, never needing a cleanroom, with parallel hatches and overclocking; EMI shows the copies
        // with the product as a ghost ingredient
    }

    private static void addLenslessCopy(GTRecipeBuilder builder, Consumer<FinishedRecipe> provider) {
        List<Content> outputs = builder.output.get(ItemRecipeCapability.CAP);
        ItemStack product = outputs == null || outputs.isEmpty() ? ItemStack.EMPTY : product(outputs.get(0));
        if (product.isEmpty()) return;
        GTRecipeBuilder copy = RECIPE_TYPE.copyFrom(builder);
        // no cleanroom or any other condition of the laser engraver recipe
        copy.conditions.clear();
        List<Content> inputs = copy.input.get(ItemRecipeCapability.CAP);
        if (inputs != null) inputs.removeIf(content -> content.chance() == 0);
        if (inputs != null && inputs.stream().anyMatch(AdvancedChipEngraving::isWafer)) {
            copy.duration(builder.duration * WAFER_MULTIPLIER);
            copy.output.get(ItemRecipeCapability.CAP).replaceAll(content ->
                    content.copyChanced(ItemRecipeCapability.CAP, ContentModifier.multiplier(WAFER_MULTIPLIER)));
        }
        copy.input(GhostIngredientRecipeCapability.CAP, new ItemGhostIngredient(product));
        copy.save(provider);
    }

    /**
     * The item an output gives, if it is written as items only: those need no tags, while this runs when recipes load,
     * before the registries have the new ones, and getItems would keep what it found. Laser engraver outputs are
     * single items; anything else gives none, and the recipe no copy.
     */
    private static ItemStack product(Content output) {
        if (!(output.content() instanceof Ingredient ingredient)) return ItemStack.EMPTY;
        Ingredient inner = ingredient instanceof SizedIngredient sized ? sized.getInner() : ingredient;
        if (inner.getClass() != Ingredient.class) return ItemStack.EMPTY;
        for (Ingredient.Value value : ((IngredientAccessor) inner).getValues()) {
            if (!(value instanceof Ingredient.ItemValue)) return ItemStack.EMPTY;
        }
        ItemStack[] items = inner.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0].copyWithCount(1);
    }

    /**
     * An input written as an item or a tag whose name ends in {@code _wafer} (GT's silicon, phosphorus, naquadah and
     * neutronium wafers), read from how its values are written like {@link #product}.
     */
    private static boolean isWafer(Content input) {
        if (!(input.content() instanceof Ingredient ingredient)) return false;
        Ingredient inner = ingredient instanceof SizedIngredient sized ? sized.getInner() : ingredient;
        if (inner.getClass() != Ingredient.class) return false;
        for (Ingredient.Value value : ((IngredientAccessor) inner).getValues()) {
            JsonObject json = value.serialize();
            String id = json.has("item") ? GsonHelper.getAsString(json, "item") :
                    json.has("tag") ? GsonHelper.getAsString(json, "tag") : "";
            if (id.endsWith("_wafer")) return true;
        }
        return false;
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id(ID))
                .inputItems(GCYMMachines.LARGE_ENGRAVING_LASER, 1)
                .inputItems(GTItems.EMITTER_IV, 64)
                .inputItems(CustomTags.ZPM_CIRCUITS, 16)
                .inputItems(CASING_LASER_SAFE_ENGRAVING.asItem(), 64)
                .inputItems(plateDouble, TungstenSteel, 64)
                .inputFluids(SolderingAlloy.getFluid(L * 32))
                .outputItems(MACHINE)
                .duration(1200)
                .EUt(VA[IV])
                .save(provider);
    }
}
