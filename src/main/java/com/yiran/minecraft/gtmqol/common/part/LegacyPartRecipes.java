package com.yiran.minecraft.gtmqol.common.part;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.EXTRUDER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORMING_PRESS_RECIPES;

/**
 * Part recipes GTCEu v8 removed, back as they were before: the rod, bolt, gear, small gear and rotor extruder molds
 * with their extruder recipes (v8 makes rods on the lathe, gears in the forming press, rotors in the welder). Plus a
 * long rod mold, which GTCEu only has the texture of. GTCEu still ships all the textures.
 *
 * <p>Rotors count as 4 ingots again (v8: 4 plates + a ring = 17/4), so extruding 4 ingots and recycling the rotor
 * doesn't make metal. That applies to every rotor, welded ones included.</p>
 */
public final class LegacyPartRecipes {

    public static ItemEntry<Item> ROD;
    public static ItemEntry<Item> LONG_ROD;
    public static ItemEntry<Item> BOLT;
    public static ItemEntry<Item> GEAR;
    public static ItemEntry<Item> SMALL_GEAR;
    public static ItemEntry<Item> ROTOR;

    private LegacyPartRecipes() {}

    public static void init() {
        ROD = mold("rod", "Rod");
        LONG_ROD = mold("long_rod", "Long Rod");
        BOLT = mold("bolt", "Bolt");
        GEAR = mold("gear", "Gear");
        SMALL_GEAR = mold("small_gear", "Small Gear");
        ROTOR = mold("rotor", "Rotor");
        rotor.materialAmount(M * 4);
    }

    private static ItemEntry<Item> mold(String shape, String name) {
        String id = shape + "_extruder_mold";
        return GTMQoLAddon.registrate().item(id, Item::new)
                .lang("Extruder Mold (" + name + ")")
                .model((ctx, prov) -> prov.generated(ctx, GTCEu.id("item/" + id)))
                .onRegister(GTItems.materialInfo(new ItemMaterialInfo(new MaterialStack(GTMaterials.Steel, M * 4))))
                .register();
    }

    public static void addRecipes(RecipeOutput provider) {
        addMoldRecipes(provider);
        for (Material material : GTRegistries.MATERIALS) {
            if (material.hasFlag(DISABLE_MATERIAL_RECIPES)) continue;
            addRodRecipes(provider, material);
            addBoltRecipes(provider, material);
            addGearRecipes(provider, material);
            addSmallGearRecipes(provider, material);
            addRotorRecipes(provider, material);
        }
    }

    /**
     * With a file, like GTCEu's molds. v8 gave the old rod and rotor patterns on the empty mold to pipe molds, so those
     * two start from the wire and ring molds now; bolt, gear and small gear are the old patterns.
     */
    private static void addMoldRecipes(RecipeOutput provider) {
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_rod"), ROD.asStack(),
                "   ", " Sx", "   ", 'S', GTItems.SHAPE_EXTRUDER_WIRE.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_long_rod"), LONG_ROD.asStack(),
                "   ", " S ", " x ", 'S', ROD.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_bolt"), BOLT.asStack(),
                "x  ", " S ", "   ", 'S', ROD.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_gear"), GEAR.asStack(),
                "x  ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_gear_small"),
                SMALL_GEAR.asStack(), " x ", " S ", "   ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack());
        VanillaRecipeHelper.addStrictShapedRecipe(provider, GTMQoL.id("shape_extruder_rotor"), ROTOR.asStack(),
                "   ", " S ", "x  ", 'S', GTItems.SHAPE_EXTRUDER_RING.asStack());

        // as GTCEu does for its own molds
        for (ItemEntry<Item> mold : List.of(ROD, LONG_ROD, BOLT, GEAR, SMALL_GEAR, ROTOR)) {
            FORMING_PRESS_RECIPES.recipeBuilder(GTMQoL.id("copy_shape_" + mold.getId().getPath()))
                    .duration(120).EUt(22)
                    .notConsumable(mold)
                    .inputItems(GTItems.SHAPE_EMPTY)
                    .outputItems(mold)
                    .save(provider);
        }
    }

    /** 1 ingot: 2 rods, or a long rod (new, same time and power). */
    private static void addRodRecipes(RecipeOutput provider, Material material) {
        if (!material.shouldGenerateRecipesFor(ingot) || !material.hasProperty(PropertyKey.INGOT) ||
                material.hasFlag(NO_WORKING)) {
            return;
        }
        Material output = unmagnetized(material);
        int duration = (int) material.getMass() * 2;
        long eut = 6L * voltageMultiplier(material);
        if (material.hasFlag(GENERATE_ROD)) {
            extrude(provider, material, "to_rod", ingot, 1, ROD, ChemicalHelper.get(rod, output, 2), duration, eut);
        }
        if (material.shouldGenerateRecipesFor(rodLong)) {
            extrude(provider, material, "to_long_rod", ingot, 1, LONG_ROD, ChemicalHelper.get(rodLong, output),
                    duration, eut);
        }
    }

    private static void addBoltRecipes(RecipeOutput provider, Material material) {
        if (!material.shouldGenerateRecipesFor(bolt) || !material.hasProperty(PropertyKey.DUST) ||
                ChemicalHelper.get(ingot, material).isEmpty()) {
            return;
        }
        ItemStack bolts = ChemicalHelper.get(bolt, unmagnetized(material), 8);
        extrude(provider, material, "ingot_to_bolt", ingot, 1, BOLT, bolts, 15, VA[MV]);
        if (material.hasFlag(NO_SMASHING)) {
            extrude(provider, material, "dust_to_bolt", dust, 1, BOLT, bolts, 15, VA[MV]);
        }
    }

    private static void addGearRecipes(RecipeOutput provider, Material material) {
        if (!material.shouldGenerateRecipesFor(gear) || !material.hasProperty(PropertyKey.DUST) ||
                !material.hasProperty(PropertyKey.INGOT)) {
            return;
        }
        ItemStack stack = ChemicalHelper.get(gear, unmagnetized(material));
        int duration = (int) material.getMass() * 5;
        long eut = 8L * voltageMultiplier(material);
        extrude(provider, material, "ingot_to_gear", ingot, 4, GEAR, stack, duration, eut);
        if (material.hasFlag(NO_SMASHING)) {
            extrude(provider, material, "dust_to_gear", dust, 4, GEAR, stack, duration, eut);
        }
    }

    private static void addSmallGearRecipes(RecipeOutput provider, Material material) {
        if (!material.shouldGenerateRecipesFor(gearSmall) || !material.hasProperty(PropertyKey.DUST) ||
                !material.hasFlag(GENERATE_PLATE) || !material.hasFlag(GENERATE_ROD)) {
            return;
        }
        ItemStack stack = ChemicalHelper.get(gearSmall, unmagnetized(material));
        int duration = (int) material.getMass();
        extrude(provider, material, "ingot_to_small_gear", ingot, 1, SMALL_GEAR, stack, duration, hotEUt(material));
        if (material.hasFlag(NO_SMASHING)) {
            extrude(provider, material, "dust_to_small_gear", dust, 1, SMALL_GEAR, stack, duration, hotEUt(material));
        }
    }

    private static void addRotorRecipes(RecipeOutput provider, Material material) {
        if (!material.shouldGenerateRecipesFor(rotor) || !material.hasProperty(PropertyKey.INGOT)) return;
        ItemStack stack = ChemicalHelper.get(rotor, material);
        int duration = (int) material.getMass() * 4;
        extrude(provider, material, "ingot_to_rotor", ingot, 4, ROTOR, stack, duration, hotEUt(material));
        if (material.hasFlag(NO_SMASHING)) {
            extrude(provider, material, "dust_to_rotor", dust, 4, ROTOR, stack, duration, hotEUt(material));
        }
    }

    private static void extrude(RecipeOutput provider, Material material, String name, TagPrefix input, int count,
                                ItemEntry<Item> mold, ItemStack output, int duration, long eut) {
        if (output.isEmpty()) return;
        EXTRUDER_RECIPES.recipeBuilder(GTMQoL.id("extrude_" + material.getName() + "_" + name))
                .inputItems(input, material, count)
                .notConsumable(mold)
                .outputItems(output)
                .duration(duration)
                .EUt(eut)
                .save(provider);
    }

    /** Magnetic materials come out as what they macerate into, as in GTCEu. */
    private static Material unmagnetized(Material material) {
        if (!material.hasFlag(IS_MAGNETIC) || !material.hasProperty(PropertyKey.INGOT)) return material;
        Material into = material.getProperty(PropertyKey.INGOT).getMacerateInto();
        return into == null ? material : into;
    }

    private static long voltageMultiplier(Material material) {
        return material.getBlastTemperature() >= 2800 ? VA[LV] : VA[ULV];
    }

    private static long hotEUt(Material material) {
        return material.getBlastTemperature() >= 2800 ? 256 : 64;
    }
}
