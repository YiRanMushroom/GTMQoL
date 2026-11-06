package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.integration.ae2.AECrafterPattern;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.OC_NON_PERFECT;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.explosion;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/**
 * Pattern crafter: an electric single block in every tier that runs crafting table, stonecutter and smithing recipes
 * from a
 * {@link CraftingPattern} or an AE2 pattern. There are no registered recipes: {@link PatternCrafterLogic} makes one from each pattern, so
 * overclocking and the rest work as in any other machine.
 */
// Untested: JEI's fill button puts crafting, stonecutting and smithing recipes in the encoding window (dev runs EMI);
// exact matches durability, substitutes accept any; a tool wears by one craft per parallel, a run stops at what it has
// left, then the next tool takes over; the duplicates button keeps a smithing template and makes one; 9 patterns
public final class PatternCrafter {

    private static final String KEY = "gtmqol.machine.pattern_crafter.";

    /**
     * One craft at LV; overclocks as usual.
     */
    static final int DURATION = 20;
    static final int EU_PER_TICK = VA[LV];

    public static ItemEntry<CraftingPatternItem> BLANK_PATTERN;
    public static ItemEntry<CraftingPatternItem> PATTERN;
    public static GTRecipeTypeEntry RECIPE_TYPE;
    public static MachineEntry<MachineDefinition>[] MACHINES;

    private PatternCrafter() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        var registrate = GTMQoLAddon.registrate();
        var behavior = new CraftingPatternBehavior();
        BLANK_PATTERN = registrate.item("blank_crafting_pattern", CraftingPatternItem::new)
                .lang("Blank Crafting Pattern")
                .onRegister(attach(behavior))
                .register();
        PATTERN = registrate.item("crafting_pattern", CraftingPatternItem::new)
                .lang("Crafting Pattern")
                .properties(properties -> properties.stacksTo(1))
                .onRegister(attach(behavior))
                .register();
        // 9 materials and 9 patterns; the crafted item and up to 9 different things given back
        RECIPE_TYPE = registrate.recipeType("pattern_crafter", GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(CraftingPattern.SLOTS * 2, CraftingPattern.SLOTS + 1, CraftingPattern.SLOTS, 0)
                .setEUIO(IO.IN)
                .addCustomRecipeLogic(PatternCrafterLogic.INSTANCE)
                .UI(PatternCrafterUI::apply)
                .setSound(GTSoundEntries.ASSEMBLER)
                .lang("Pattern Crafter")
                .register();

        MACHINES = new MachineEntry[TIER_COUNT];
        for (int tier : ELECTRIC_TIERS) {
            MACHINES[tier] = GTMQoLAddon.tieredMachine("pattern_crafter", tier, PatternWear.FEATURE)
                    .ui(PatternCrafterUI.PANEL)
                    .langValue("%s Pattern Crafter %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeModifiers(OC_NON_PERFECT, PatternWear.LIMIT)
                    .tooltips(Component.translatable(KEY + "tooltip"))
                    .workableTiered(RECIPE_TYPE, GTCEu.id("block/machines/assembler"))
                    .tooltips(explosion())
                    .register();
        }

        registrate.addRawLang(KEY + "tooltip", "Crafts the recipe of the crafting pattern among its inputs.");
        registrate.addRawLang(PatternWear.WORN_OUT_KEY, "No tool left that can do this");
        CraftingPatternBehavior.addLang();
    }

    public static boolean isPattern(ItemStack stack) {
        return stack.is(PATTERN.get()) || (GTCEu.Mods.isAE2Loaded() && AECrafterPattern.isPattern(stack));
    }

    public static void addRecipes(RecipeOutput provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, MACHINES, "ACA", "WMW", "CTC",
                'M', HULL, 'A', ROBOT_ARM, 'C', CIRCUIT, 'W', CABLE, 'T', Items.CRAFTING_TABLE);
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("blank_crafting_pattern"), BLANK_PATTERN.asStack(4),
                " P ", "PCP", " P ", 'P', Items.PAPER, 'C', CustomTags.LV_CIRCUITS);
        MagicalAssembler.RECIPE_TYPE.recipeBuilder(GTMQoL.id("blank_crafting_pattern"))
                .inputItems(Items.PAPER, 2)
                .inputItems(CustomTags.LV_CIRCUITS)
                .circuitMeta(5)
                .outputItems(BLANK_PATTERN.asStack(4))
                .EUt(EU_PER_TICK).duration(DURATION)
                .save(provider);
        VanillaRecipeHelper.addShapelessRecipe(provider, GTMQoL.id("clear_crafting_pattern"), BLANK_PATTERN.asStack(),
                PATTERN.asStack());
    }
}
