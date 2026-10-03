package com.yiran.minecraft.gtmqol.common.circuit;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.ChatFormatting;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES;

/** Cheap ULV-EV circuits, made in the circuit assembler. ULV-HV fit in the large steam circuit assembler (MV). */
public final class ControlCircuits {

    public static ItemEntry<ControlCircuitItem> VERY_SIMPLE;
    public static ItemEntry<ControlCircuitItem> SIMPLE;
    public static ItemEntry<ControlCircuitItem> BASIC;
    public static ItemEntry<ControlCircuitItem> ADVANCED;
    public static ItemEntry<ControlCircuitItem> ELITE;

    private ControlCircuits() {}

    public static void init() {
        VERY_SIMPLE = register("very_simple", "Very Simple", "A very simple circuit board", ULV, ChatFormatting.WHITE);
        SIMPLE = register("simple", "Simple", "A simple circuit board", LV, ChatFormatting.GRAY);
        BASIC = register("basic", "Basic", "A basic circuit board", MV, ChatFormatting.GOLD);
        ADVANCED = register("advanced", "Advanced", "An advanced circuit board", HV, ChatFormatting.YELLOW);
        ELITE = register("elite", "Elite", "An elite circuit board", EV, ChatFormatting.DARK_GRAY);
    }

    private static ItemEntry<ControlCircuitItem> register(String id, String name, String description, int tier,
                                                          ChatFormatting tierColor) {
        Component[] tooltip = {
                Component.translatable("item.gtmqol.%s_control_circuit.tooltip".formatted(id)),
                Component.literal(VN[tier] + "-Tier").withStyle(tierColor)
        };
        GTMQoLAddon.registrate().addRawLang("item.gtmqol.%s_control_circuit.tooltip".formatted(id), description);
        return GTMQoLAddon.registrate()
                .item(id + "_control_circuit", p -> new ControlCircuitItem(p, tooltip))
                .lang(name + " Control Circuit")
                .tag(CustomTags.CIRCUITS_ARRAY[tier])
                .register();
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("very_simple_control_circuit"))
                .circuitMeta(14)
                .inputItems(plate, Steel)
                .inputItems(dust, Redstone)
                .inputFluids(Glue, 20)
                .outputItems(VERY_SIMPLE, 4)
                .duration(40)
                .EUt(7)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("simple_control_circuit"))
                .circuitMeta(14)
                .inputItems(VERY_SIMPLE, 2)
                .inputItems(plate, Iron)
                .inputItems(dust, RedAlloy)
                .inputFluids(Glue, 20)
                .outputItems(SIMPLE, 4)
                .duration(80)
                .EUt(16)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("basic_control_circuit"))
                .circuitMeta(14)
                .inputItems(Items.PAPER)
                .inputItems(SIMPLE, 4)
                .inputItems(plate, WroughtIron)
                .inputItems(dustSmall, Diamond)
                .inputFluids(Glue, 20)
                .outputItems(BASIC, 4)
                .duration(160)
                .EUt(30)
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("advanced_control_circuit"))
                .circuitMeta(14)
                .inputItems(GTItems.COATED_BOARD)
                .inputItems(BASIC, 2)
                .inputItems(plate, Steel)
                .inputItems(dustSmall, Obsidian)
                .inputItems(screw, RedAlloy)
                .inputFluids(Glue, 20)
                .outputItems(ADVANCED, 2)
                .duration(80)
                .EUt(VA[MV])
                .save(provider);

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("elite_control_circuit"))
                .inputItems(GTItems.GOOD_CIRCUIT_BOARD)
                .inputItems(ADVANCED, 2)
                .inputItems(foil, RedAlloy, 8)
                .inputFluids(SolderingAlloy, L)
                .outputItems(ELITE, 2)
                .duration(200)
                .EUt(VA[HV])
                .save(provider);
    }

    public static class ControlCircuitItem extends Item {

        private final Component[] tooltip;

        public ControlCircuitItem(Properties properties, Component[] tooltip) {
            super(properties);
            this.tooltip = tooltip;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                    TooltipFlag isAdvanced) {
            tooltipComponents.addAll(List.of(tooltip));
        }
    }
}
