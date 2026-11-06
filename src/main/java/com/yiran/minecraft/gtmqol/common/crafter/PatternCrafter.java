package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.integration.ae2.AECrafterPattern;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.function.Consumer;

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
// Untested: everything on 1.20: encoding window (three modes, Clear, tooltip), EMI's and JEI's fill buttons, JEI and EMI
// stations; crafting from patterns; tools unworn, worn ones accepted; substitutes; buckets as fluid or item; AE2
// crafting, stonecutting and smithing patterns run, processing patterns don't; taking the pattern out stops the
// machine; a reload while crafting; an encoded pattern crafted alone gives a blank one
public final class PatternCrafter {

    private static final String KEY = "gtmqol.machine.pattern_crafter.";

    /** One craft at LV; overclocks as usual. */
    static final int DURATION = 20;
    static final int EU_PER_TICK = VA[LV];

    public static ItemEntry<CraftingPatternItem> BLANK_PATTERN;
    public static ItemEntry<CraftingPatternItem> PATTERN;
    public static GTRecipeType RECIPE_TYPE;
    public static MachineDefinition[] MACHINES;

    private PatternCrafter() {}

    public static void initItems() {
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
        CraftingPatternBehavior.addLang();
    }

    /** From GTCEu's recipe type {@code RegisterEvent}. */
    public static void initRecipeType() {
        // 9 materials and the pattern
        RECIPE_TYPE = GTRecipeTypes.register(GTMQoL.id("pattern_crafter"), GTRecipeTypes.ELECTRIC)
                .setMaxIOSize(CraftingPattern.SLOTS + 1, CraftingPattern.SLOTS, CraftingPattern.SLOTS, 0)
                .setEUIO(IO.IN)
                .addCustomRecipeLogic(PatternCrafterLogic.INSTANCE)
                .UI(PatternCrafterUI::apply)
                .setSound(GTSoundEntries.ASSEMBLER);
        GTMQoLAddon.registrate().addRawLang(RECIPE_TYPE.registryName.toLanguageKey(), "Pattern Crafter");
    }

    /** From GTCEu's machine {@code RegisterEvent}. */
    public static void initMachines() {
        MACHINES = new MachineDefinition[TIER_COUNT];
        for (int tier : ELECTRIC_TIERS) {
            MACHINES[tier] = GTMQoLAddon.tieredMachine("pattern_crafter", tier)
                    .ui(PatternCrafterUI.PANEL)
                    .langValue("%s Pattern Crafter %s".formatted(VLVH[tier], VLVT[tier]))
                    .recipeModifier(OC_NON_PERFECT)
                    .tooltips(Component.translatable(KEY + "tooltip"))
                    .workableTiered(RECIPE_TYPE, GTCEu.id("block/machines/assembler"))
                    .tooltips(explosion())
                    .register();
        }
        GTMQoLAddon.registrate().addRawLang(KEY + "tooltip", "Crafts the recipe of the crafting pattern among its inputs.");
    }

    public static boolean isPattern(ItemStack stack) {
        return stack.is(PATTERN.get()) || (GTCEu.Mods.isAE2Loaded() && AECrafterPattern.isPattern(stack));
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, MACHINES, "ACA", "WMW", "CTC",
                'M', HULL, 'A', ROBOT_ARM, 'C', CIRCUIT, 'W', CABLE, 'T', Items.CRAFTING_TABLE);
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("blank_crafting_pattern"), BLANK_PATTERN.asStack(4),
                " P ", "PCP", " P ", 'P', Items.PAPER, 'C', CustomTags.LV_CIRCUITS);
        VanillaRecipeHelper.addShapelessRecipe(provider, GTMQoL.id("clear_crafting_pattern"), BLANK_PATTERN.asStack(),
                PATTERN.asStack());
    }
}
