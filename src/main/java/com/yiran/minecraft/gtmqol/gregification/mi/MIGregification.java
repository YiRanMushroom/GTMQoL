package com.yiran.minecraft.gtmqol.gregification.mi;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeTypeUILayout;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.recipe.gui.GTRecipeUIModifiers;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.data.tag.MaterialAliasTags;
import com.yiran.minecraft.gtmqol.gregification.ForeignMachineType;
import com.yiran.minecraft.gtmqol.gregification.ForeignRecipeConverter;
import com.yiran.minecraft.gtmqol.gregification.MultiblockShape;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static java.util.Map.entry;

/**
 * Modern Industrialization's machine recipe types. Recipes run at {@code VA[LV]}, as long as it takes to use MI's
 * total energy.
 */
public final class MIGregification {

    private static final String MI = "modern_industrialization";
    private static final int MULTIBLOCK_SLOTS = 6;

    // MI's guidebook, kept when crafting a machine
    private static final ResourceLocation GUIDEBOOK = ResourceLocation.fromNamespaceAndPath(MI, "guidebook");

    /**
     * @param model       GTCEu machine whose model is used
     * @param counterpart crafted from this machine, {@code %s} is the tier
     */
    private record SingleBlock(int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, String model,
                               String counterpart, @Nullable Holder<SoundEntry> sound,
                               UnaryOperator<GTRecipeTypeUILayout.Builder> ui) {

        SingleBlock(int itemInputs, int itemOutputs, int fluidInputs, int fluidOutputs, String model,
                    @Nullable Holder<SoundEntry> sound, UnaryOperator<GTRecipeTypeUILayout.Builder> ui) {
            this(itemInputs, itemOutputs, fluidInputs, fluidOutputs, model, "gtceu:%s_" + model, sound, ui);
        }
    }

    private record Multiblock(boolean itemInputs, boolean itemOutputs, boolean fluidInputs, boolean fluidOutputs,
                              MultiblockShape shape, String counterpart, @Nullable Holder<SoundEntry> sound,
                              UnaryOperator<GTRecipeTypeUILayout.Builder> ui) {}

    // slot counts of MI's own machines; sound and progress bar of the GT counterpart
    private static final Map<String, SingleBlock> SINGLE_BLOCKS = Map.ofEntries(
            entry("assembler", new SingleBlock(9, 3, 2, 0, "assembler",
                    GTSoundEntries.ASSEMBLER, progress(GTGuiTextures.PROGRESS_ASSEMBLER))),
            entry("centrifuge", new SingleBlock(1, 4, 1, 4, "centrifuge",
                    GTSoundEntries.CENTRIFUGE, progress(GTGuiTextures.PROGRESS_EXTRACT))),
            entry("chemical_reactor", new SingleBlock(3, 3, 3, 3, "chemical_reactor",
                    GTSoundEntries.CHEMICAL, progress(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))),
            entry("compressor", new SingleBlock(1, 1, 0, 0, "compressor",
                    GTSoundEntries.COMPRESSOR, progress(GTGuiTextures.PROGRESS_COMPRESS))),
            entry("cutting_machine", new SingleBlock(1, 1, 1, 0, "cutter",
                    GTSoundEntries.CUT, progress(GTGuiTextures.PROGRESS_CUTTER))),
            entry("distillery", new SingleBlock(0, 0, 1, 1, "distillery",
                    GTSoundEntries.BOILER, progress(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))),
            entry("electrolyzer", new SingleBlock(1, 4, 1, 4, "electrolyzer",
                    GTSoundEntries.ELECTROLYZER, progress(GTGuiTextures.PROGRESS_EXTRACT))),
            entry("furnace", new SingleBlock(1, 1, 0, 0, "electric_furnace",
                    GTSoundEntries.FURNACE, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("macerator", new SingleBlock(1, 4, 0, 0, "macerator",
                    GTSoundEntries.MACERATOR, progress(GTGuiTextures.PROGRESS_MACERATE))),
            entry("mixer", new SingleBlock(4, 2, 2, 2, "mixer",
                    GTSoundEntries.MIXER, MIGregification::mixerProgress)),
            entry("packer", new SingleBlock(3, 1, 0, 0, "packer",
                    GTSoundEntries.ASSEMBLER, progress(GTGuiTextures.PROGRESS_PACKER))),
            entry("polarizer", new SingleBlock(2, 1, 0, 0, "polarizer",
                    GTSoundEntries.ARC, progress(GTGuiTextures.PROGRESS_MAGNET))),
            // GT's packer is taken by MI's packer
            entry("unpacker", new SingleBlock(1, 2, 0, 0, "packer", "gtmqol:%s_mi_packer",
                    GTSoundEntries.ASSEMBLER, progress(GTGuiTextures.PROGRESS_PACKER))),
            entry("wiremill", new SingleBlock(1, 1, 0, 0, "wiremill",
                    GTSoundEntries.MOTOR, progress(GTGuiTextures.PROGRESS_WIREMILL))));

    // no GT counterpart: crafted from MI's own machine, no sound
    private static final Map<String, Multiblock> MULTIBLOCKS = Map.ofEntries(
            entry("blast_furnace", new Multiblock(true, true, true, true, MultiblockShape.ELECTRIC_BLAST_FURNACE,
                    "gtceu:electric_blast_furnace", GTSoundEntries.FURNACE,
                    builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                            .addRecipeUIModifier(GTRecipeUIModifiers.TEMP_COIL_INFO))),
            entry("coke_oven", new Multiblock(true, true, false, true, MultiblockShape.GENERIC,
                    "gtceu:pyrolyse_oven", GTSoundEntries.FIRE, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("distillation_tower", new Multiblock(false, false, true, true, MultiblockShape.DISTILLATION_TOWER,
                    "gtceu:distillation_tower", GTSoundEntries.CHEMICAL,
                    progress(GTGuiTextures.PROGRESS_ARROW_MULTIPLE))),
            entry("fusion_reactor", new Multiblock(false, false, true, true, MultiblockShape.GENERIC,
                    "gtceu:luv_fusion_reactor", GTSoundEntries.ARC, progress(GTGuiTextures.PROGRESS_FUSION))),
            entry("heat_exchanger", new Multiblock(true, true, true, true, MultiblockShape.GENERIC,
                    MI + ":heat_exchanger", null, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("implosion_compressor", new Multiblock(true, true, false, false,
                    MultiblockShape.IMPLOSION_COMPRESSOR, "gtceu:implosion_compressor",
                    // same as GT's
                    Holder.direct(new ExistingSoundEntry(SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS)),
                    progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("oil_drilling_rig", new Multiblock(true, false, false, true, MultiblockShape.GENERIC,
                    MI + ":oil_drilling_rig", null, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("pressurizer", new Multiblock(true, false, true, true, MultiblockShape.GENERIC,
                    MI + ":pressurizer", null, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("quarry", new Multiblock(true, true, false, false, MultiblockShape.GENERIC,
                    MI + ":electric_quarry", null, progress(GTGuiTextures.PROGRESS_ARROW))),
            entry("vacuum_freezer", new Multiblock(true, true, true, true, MultiblockShape.VACUUM_FREEZER,
                    "gtceu:vacuum_freezer", GTSoundEntries.COOLING, progress(GTGuiTextures.PROGRESS_ARROW))));

    private MIGregification() {}

    public static List<ForeignMachineType> types() {
        List<ForeignMachineType> types = new ArrayList<>();
        for (MachineRecipeType recipeType : MIMachineRecipeTypes.getRecipeTypes()) {
            // not the registry: MI's recipe types may not be registered yet when we declare
            ResourceLocation id = recipeType.getId();
            String path = id.getPath();
            String name = "mi_" + path;
            String englishName = "MI " + englishName(path);
            SingleBlock single = SINGLE_BLOCKS.get(path);
            Multiblock multi = MULTIBLOCKS.get(path);
            if (single != null) {
                types.add(ForeignMachineType.singleBlock(name, englishName, () -> recipeType,
                        MIGregification::convert,
                        single.itemInputs, single.itemOutputs, single.fluidInputs, single.fluidOutputs,
                        single.sound, single.ui, GUIDEBOOK,
                        tier -> ResourceLocation.parse(
                                single.counterpart.formatted(VN[tier].toLowerCase(Locale.ROOT))),
                        GTCEu.id("block/machines/" + single.model)));
            } else if (multi != null) {
                ForeignRecipeConverter converter = path.equals("blast_furnace") ?
                        MIGregification::convertBlastFurnace : MIGregification::convert;
                types.add(ForeignMachineType.multiblock(name, englishName, () -> recipeType, converter,
                        multi.itemInputs ? MULTIBLOCK_SLOTS : 0, multi.itemOutputs ? MULTIBLOCK_SLOTS : 0,
                        multi.fluidInputs ? MULTIBLOCK_SLOTS : 0, multi.fluidOutputs ? MULTIBLOCK_SLOTS : 0,
                        multi.sound, multi.ui, GUIDEBOOK, ResourceLocation.parse(multi.counterpart),
                        multi.shape));
            } else {
                GTMQoL.LOGGER.info("Not gregifying MI recipe type {}: unknown machine", id);
            }
        }
        return types;
    }

    private static boolean convert(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!(holder.value() instanceof MachineRecipe recipe)) return false;
        // e.g. dimension or adjacent block requirements, which GT machines can't check
        if (!recipe.conditions.isEmpty()) return false;

        for (var input : recipe.itemInputs) {
            if (input.ingredient().hasNoItems()) return false;
            withChance(builder, input.probability(), true,
                    () -> builder.inputItems(new SizedIngredient(input.ingredient(), input.amount())));
        }
        for (var input : recipe.fluidInputs) {
            if (input.fluid().hasNoFluids()) return false;
            withChance(builder, input.probability(), true, () -> builder
                    .inputFluids(new SizedFluidIngredient(widenToGT(input.fluid()), (int) input.amount())));
        }
        for (var output : recipe.itemOutputs) {
            withChance(builder, output.probability(), false, () -> builder.outputItems(output.getStack()));
        }
        for (var output : recipe.fluidOutputs) {
            withChance(builder, output.probability(), false,
                    () -> builder.outputFluids(new FluidStack(toGT(output.fluid()), (int) output.amount())));
        }
        // same total energy as in MI (1 MI EU = 1 GT EU), at VA[LV]
        builder.EUt(VA[LV]).duration((int) Math.max(1, Math.ceilDiv(recipe.getTotalEu(), VA[LV])));
        return true;
    }

    /**
     * MI's blast furnace takes recipes up to its coil tier's max EU/t. The recipe needs the temperature of the GT coil
     * with the same index (cupronickel, kanthal, then nichrome... for KubeJS-added MI tiers).
     */
    private static boolean convertBlastFurnace(RecipeHolder<?> holder, GTRecipeBuilder builder) {
        if (!convert(holder, builder)) return false;
        long eu = ((MachineRecipe) holder.value()).eu;
        int tier = (int) ElectricBlastFurnaceBlockEntity.tiers.stream().filter(t -> t.maxBaseEu() < eu).count();
        CoilBlock.CoilType[] coils = CoilBlock.CoilType.values();
        builder.blastFurnaceTemp(coils[Math.min(tier, coils.length - 1)].getCoilTemperature());
        return true;
    }

    /**
     * The GT material whose name matches the fluid's id path ({@code modern_industrialization:hydrogen} ->
     * {@code gtceu:hydrogen}), going through {@link MaterialAliasTags#ALIASES} for spellings GT doesn't use. A guess
     * by name only.
     */
    private static @Nullable Material gtMaterial(Fluid fluid) {
        String name = BuiltInRegistries.FLUID.getKey(fluid).getPath();
        for (var alias : MaterialAliasTags.ALIASES.entrySet()) {
            if (alias.getValue().equals(name)) name = alias.getKey();
        }
        var material = GTRegistries.MATERIALS.getOptional(GTCEu.id(name));
        return material.filter(Material::hasFluid).orElse(null);
    }

    /** A single-fluid input also accepts the same-named GT material's fluid tag. */
    private static FluidIngredient widenToGT(FluidIngredient ingredient) {
        FluidStack[] stacks = ingredient.getStacks();
        if (stacks.length != 1) return ingredient;
        Material material = gtMaterial(stacks[0].getFluid());
        if (material == null || material.getFluid() == stacks[0].getFluid()) return ingredient;
        return CompoundFluidIngredient.of(ingredient, FluidIngredient.tag(material.getFluidTag()));
    }

    /** Outputs the same-named GT material's fluid instead, so it stacks with GT's. */
    private static Fluid toGT(Fluid fluid) {
        Material material = gtMaterial(fluid);
        return material == null ? fluid : material.getFluid();
    }

    /**
     * MI probabilities: 1 always, 0 for inputs means not consumed (a catalyst), anything else is a chance.
     */
    private static void withChance(GTRecipeBuilder builder, float probability, boolean input, Runnable add) {
        if (probability >= 1) {
            add.run();
            return;
        }
        if (probability <= 0 && !input) return;
        int chance = probability <= 0 ? 0 : Math.clamp(Math.round(probability * 10000), 1, 10000);
        int previous = builder.chance;
        builder.chance = chance;
        try {
            add.run();
        } finally {
            builder.chance = previous;
        }
    }

    private static UnaryOperator<GTRecipeTypeUILayout.Builder> progress(ProgressBarTextureSet texture) {
        return builder -> builder.setProgressBar(texture);
    }

    // same as GT's mixer
    private static GTRecipeTypeUILayout.Builder mixerProgress(GTRecipeTypeUILayout.Builder builder) {
        return builder.setProgressBarSupplier((layout, value, machine) -> new CircularProgressDrawable()
                .emptyTexture(GTGuiTextures.PROGRESS_MIXER[0])
                .filledTexture(GTGuiTextures.PROGRESS_MIXER[1])
                .clockwise()
                .asWidget()
                .value(value));
    }

    private static String englishName(String path) {
        StringBuilder name = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }
}
