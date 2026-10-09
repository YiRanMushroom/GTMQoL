package com.yiran.minecraft.gtmqol.common.crystal;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;

/**
 * Crystal Growth Chamber: a 3 × 3 × 3 multiblock with a budding block in the middle, which makes that block's
 * shards. One recipe per budding block, told apart by a {@link CenterBlockCondition}. Each recipe also has its
 * shard as a non-consumed input (like the rock breaker's cobblestone): GTCEu's recipe lookup keeps only one recipe
 * per set of item/fluid inputs and drops the rest, so recipes with the same (or no) inputs would not even load.
 */
public final class CrystalGrowth {

    private static final String KEY = "gtmqol.multiblock.crystal_growth_chamber.";

    public static RegistryEntry<RecipeConditionType<?>, RecipeConditionType<CenterBlockCondition>> CENTER_BLOCK;
    public static GTRecipeTypeEntry RECIPE_TYPE;
    public static MachineEntry<MultiblockMachineDefinition> MACHINE;

    private CrystalGrowth() {}

    public static void init() {
        var registrate = GTMQoLAddon.registrate();
        CENTER_BLOCK = registrate.simple("center_block", GTRegistries.Keys.RECIPE_CONDITION,
                () -> new RecipeConditionType<>(CenterBlockCondition::new, CenterBlockCondition.CODEC));

        RECIPE_TYPE = registrate.recipeType("crystal_growth", GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
                .lang("Crystal Growth")
                .register();

        MACHINE = GTMQoLAddon.multiblock("crystal_growth_chamber", WorkableElectricMultiblockMachine::new)
                // the condition looks behind the controller, which only works without extended facing
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(RECIPE_TYPE)
                .recipeModifiers(OC_PERFECT_SUBTICK, BATCH_MODE)
                .tooltips(Component.translatable(KEY + "tooltip.0"), Component.translatable(KEY + "tooltip.1"))
                .appearanceBlock(CASING_STEEL_SOLID)
                // G: the middle of the four sides and the back, glass or casing
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXX", "XGX", "XXX")
                        .slice("XGX", "GCG", "XGX")
                        .slice("XXX", "XSX", "XXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(CASING_STEEL_SOLID.get())
                                .and(autoAbilities(definition.getRecipeTypes()))
                                .and(autoAbilities(true, false, false)))
                        .where('G', blocks(CASING_TEMPERED_GLASS.get()).or(blocks(CASING_STEEL_SOLID.get())))
                        .where('C', any())
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                        GTCEu.id("block/multiblock/implosion_compressor"))
                .langValue("Crystal Growth Chamber")
                .register();

        registrate.addRawLang(CenterBlockCondition.KEY, "Center block:");
        registrate.addRawLang(CenterBlockCondition.KEY + ".tooltip", "Center block must be %s");
        registrate.addRawLang(KEY + "tooltip.0", "Grows the shards of the budding block in its middle.");
        registrate.addRawLang(KEY + "tooltip.1", "Needs one of those shards in an input bus, which is not consumed.");
    }

    public static void addRecipes(RecipeOutput provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTMQoL.id("crystal_growth_chamber"),
                MACHINE.asStack(), "GCG", "PHP", "GCG",
                'G', CASING_TEMPERED_GLASS.asItem(),
                'C', CustomTags.LV_CIRCUITS,
                'P', GTItems.ELECTRIC_PISTON_LV.asStack(),
                'H', GTMachines.HULL[LV].asStack());

        addCrystal(provider, "ae2", "flawless_budding_quartz", "certus_quartz_crystal");

        // <ns>:budding_<x> -> <ns>:<x>_shard, in any namespace: vanilla amethyst and every GeOre crystal
        // (GeOreBlockReg registers them under exactly these names). Recipes are generated at runtime, so this sees
        // whatever is actually registered.
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            if (!id.getPath().startsWith("budding_")) continue;
            String name = id.getPath().substring("budding_".length());
            addCrystal(provider, id.getNamespace(), id.getPath(), name + "_shard");
        }
    }

    /** Does nothing if either id is not registered (mod not loaded). */
    private static void addCrystal(RecipeOutput provider, String namespace, String budding, String shard) {
        var block = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, budding));
        var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, shard));
        if (block.isEmpty() || item.isEmpty()) return;
        addCrystal(provider, block.get(), item.get());
    }

    private static void addCrystal(RecipeOutput provider, Block budding, Item shard) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(budding);
        RECIPE_TYPE.recipeBuilder(GTMQoL.id(id.getNamespace() + "_" + id.getPath()))
                .notConsumable(shard)
                .addCondition(new CenterBlockCondition(budding))
                // a full-grown cluster drops 4
                .outputItems(shard, 4)
                .duration(200)
                .EUt(VA[LV])
                .save(provider);
    }
}
