package com.yiran.minecraft.gtmqol.common.implosion;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeModifiers.*;

/**
 * Electric implosion compressor, ported from the 1.19 version: every implosion recipe that uses vanilla TNT gets a
 * copy without the TNT, four times as long, in its own recipe type. GTCEu makes four variants of each implosion
 * recipe (powderbarrel, TNT, dynamite, industrial TNT); only the TNT one is copied, so there is one electric recipe
 * per recipe.
 * <p>
 * The copies come from an {@code onSave} on the implosion type's prototype builder, which every
 * {@code recipeBuilder(id)} copies; the same hook GTCEu's {@code onRecipeBuild} uses (chemical reactor -> LCR).
 * Any existing {@code onSave} is kept and called first. GTCEu's {@code RecipeManagerLateMixin} also runs the
 * prototype's {@code onSave} on every loaded GT recipe after recipe loading, so recipes from data packs and
 * KubeJS get a copy too.
 */
public final class ElectricImplosion {

    public static GTRecipeTypeEntry RECIPE_TYPE;
    public static MachineEntry<MultiblockMachineDefinition> MACHINE;

    private ElectricImplosion() {}

    public static void init(IEventBus modBus) {
        RECIPE_TYPE = GTMQoLAddon.registrate().recipeType("electric_implosion_compressor", GTRecipeTypes.MULTIBLOCK)
                .setMaxIOSize(3, 2, 0, 0)
                .setEUIO(IO.IN)
                .UI(builder -> builder
                        .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                        .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.IMPLOSION_OVERLAY_2)
                        .setItemSlotOverlay(IO.OUT, 1, GTGuiTextures.DUST_OVERLAY))
                .setSound(new ExistingSoundEntry(SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS))
                .lang("Electric Implosion Compressor")
                .register();

        MACHINE = GTMQoLAddon.multiblock("electric_implosion_compressor", WorkableElectricMultiblockMachine::new)
                .rotationState(RotationState.ALL)
                .recipeType(RECIPE_TYPE)
                .recipeModifiers(PARALLEL_HATCH, OC_PERFECT_SUBTICK, BATCH_MODE)
                .appearanceBlock(CASING_TUNGSTENSTEEL_ROBUST)
                .pattern(definition -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                        .slice("XXXXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                        .slice("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                        .slice("XXXXX", "#GAG#", "#GAG#", "#GAG#", "#GAG#", "XXXXX")
                        .slice("XXXXX", "#PGP#", "#PGP#", "#PGP#", "#PGP#", "XXXXX")
                        .slice("XXSXX", "F###F", "F###F", "F###F", "F###F", "XXXXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('X', blocks(CASING_TUNGSTENSTEEL_ROBUST.get())
                                .and(autoAbilities(definition.getRecipeTypes()))
                                .and(autoAbilities(true, false, true)))
                        .where('P', blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                        .where('G', blocks(CASING_TEMPERED_GLASS.get()))
                        .where('F', blocks(FIREBOX_TUNGSTENSTEEL.get()))
                        .where('A', air())
                        .where('#', any())
                        .build())
                .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                        GTCEu.id("block/multiblock/implosion_compressor"))
                .langValue("Electric Implosion Compressor")
                .register();

        // The implosion type exists after the recipe type RegisterEvent; recipes are built on data reload.
        modBus.addListener(FMLCommonSetupEvent.class, event -> hookImplosionRecipes());
    }

    private static void hookImplosionRecipes() {
        GTRecipeBuilder prototype = GTRecipeTypes.IMPLOSION_RECIPES.value().getRecipeBuilder();
        BiConsumer<GTRecipeBuilder, RecipeOutput> previous = prototype.onSave;
        prototype.onSave((builder, provider) -> {
            if (previous != null) previous.accept(builder, provider);
            addElectricCopy(builder, provider);
        });
    }

    private static void addElectricCopy(GTRecipeBuilder builder, RecipeOutput provider) {
        var items = builder.input.get(ItemRecipeCapability.CAP);
        if (items == null) return;
        ItemStack tnt = new ItemStack(Items.TNT);
        boolean hasTnt = items.stream().anyMatch(content ->
                content.content() instanceof SizedIngredient ingredient && ingredient.ingredient().test(tnt));
        if (!hasTnt) return;

        String path = builder.id.getPath();
        if (path.endsWith("_tnt")) path = path.substring(0, path.length() - "_tnt".length());
        GTRecipeBuilder electric = RECIPE_TYPE.copyFrom(builder)
                .id(builder.id.withPath(path + "_electric"))
                .duration(builder.duration * 4);
        electric.input.get(ItemRecipeCapability.CAP).removeIf(content ->
                content.content() instanceof SizedIngredient ingredient && ingredient.ingredient().test(tnt));
        electric.save(provider);
    }

    public static void addRecipes(RecipeOutput provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GTMQoL.id("electric_implosion_compressor"),
                MACHINE.asStack(), "PCP", "FSF", "PCP",
                'C', CustomTags.ZPM_CIRCUITS,
                'S', GTMultiMachines.IMPLOSION_COMPRESSOR.asStack(),
                'P', GTItems.ELECTRIC_MOTOR_IV.asStack(),
                'F', GTItems.FIELD_GENERATOR_IV.asStack());
    }
}
