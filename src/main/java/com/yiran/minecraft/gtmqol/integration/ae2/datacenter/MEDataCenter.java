package com.yiran.minecraft.gtmqol.integration.ae2.datacenter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import com.yiran.minecraft.gtmqol.integration.ae2.CasingFrameBox;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixPartRender;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.ICraftingUnitType;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SolderingAlloy;

/**
 * The ME Data Center: a hollow box ({@link CasingFrameBox}) that is the crafting CPUs of an ME network. Its edges are
 * machine casings of one tier, with its ME interface among them; its faces are quartz glass; CPU frames holding AE2's
 * crafting units fill the inside, which shows as a box of stars. Only touch this class when AE2 is loaded.
 */
public final class MEDataCenter {

    /** The ME network side of the data center; exactly one. */
    public static final PartAbility INTERFACE = new PartAbility("me_data_center_interface");

    public static MachineEntry<MachineDefinition>[] CPU_FRAMES;
    public static MachineEntry<MachineDefinition> AE_INTERFACE;
    public static MachineEntry<MultiblockMachineDefinition> CONTROLLER;

    private MEDataCenter() {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void init() {
        CPU_FRAMES = new MachineEntry[TIER_COUNT];
        for (int tier = ULV; tier <= MAX; tier++) {
            int frameTier = tier;
            CPU_FRAMES[tier] = GTMQoLAddon
                    .machine(VN[tier].toLowerCase(Locale.ROOT) + "_me_data_center_cpu_frame",
                            info -> new MEDataCenterFrameMachine(info, frameTier))
                    .tier(tier)
                    .rotationState(RotationState.NONE)
                    .modelProperty(IS_FORMED, false)
                    .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/hpca/empty"))
                    .langValue(VNF[tier] + " ME Data Center CPU Frame")
                    .tooltips(Component.translatable(MEDataCenterMachine.KEY + "frame.tooltip", tier + 1))
                    .register();
        }
        AE_INTERFACE = GTMQoLAddon.machine("me_data_center_interface", MEDataCenterInterfaceMachine::new)
                .tier(IV)
                .rotationState(RotationState.ALL)
                .abilities(INTERFACE)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .langValue("ME Data Center Interface")
                .tooltips(Component.translatable(MEDataCenterMachine.KEY + "interface.tooltip"))
                .register();
        CONTROLLER = GTMQoLAddon.multiblock("me_data_center", MEDataCenterMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(GTBlocks.MACHINE_CASING_IV)
                .partAppearance((controller, part, side) -> ((MEDataCenterMachine) controller).casingState())
                .pattern(MEDataCenter::pattern)
                .allowExtendedFacing(false)
                .allowFlip(false)
                .modelProperty(CasingFrameBox.CASING_TIER, IV)
                .modelProperty(RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(CasingFrameBox.controllerModel(GTCEu.id("block/multiblock/data_bank"))
                        .andThen(b -> b.addDynamicRenderer(AssemblingMatrixPartRender::new)
                                .addDynamicRenderer(MEDataCenterStarsRender::new)))
                .langValue("ME Data Center")
                .tooltips(Component.translatable(MEDataCenterMachine.KEY + "tooltip.0"),
                        Component.translatable(MEDataCenterMachine.KEY + "tooltip.1"),
                        Component.translatable(MEDataCenterMachine.KEY + "tooltip.2"),
                        Component.translatable(MEDataCenterMachine.KEY + "tooltip.3"),
                        Component.translatable(MEDataCenterMachine.KEY + "tooltip.4"),
                        Component.translatable(MEDataCenterMachine.KEY + "tooltip.5"))
                .register();
        MEDataCenterMachine.addLang();
    }

    /**
     * What a crafting unit's item gives a CPU, or null for other items: any block built on AE2's crafting unit, so
     * other mods' bigger storages and co-processors count too.
     */
    public static @Nullable ICraftingUnitType unit(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof AbstractCraftingUnitBlock<?> block &&
                (block.type.getStorageBytes() > 0 || block.type.getAcceleratorThreads() > 0)) {
            return block.type;
        }
        return null;
    }

    public static boolean isUnit(ItemStack stack) {
        return unit(stack) != null;
    }

    public static void addRecipes(RecipeOutput provider, boolean meAssembler) {
        GTRecipeTypeEntry type = meAssembler ? AEProcessing.ME_ASSEMBLER_RECIPES : GTRecipeTypes.ASSEMBLER_RECIPES;
        var casings = CasingFrameBox.casings();
        for (int tier = ULV; tier <= MAX; tier++) {
            type.recipeBuilder(GTMQoL.id(VN[tier].toLowerCase(Locale.ROOT) + "_me_data_center_cpu_frame"))
                    .inputItems(casings.get(tier).asStack())
                    .inputItems(AEBlocks.CRAFTING_UNIT.asItem(), tier + 1)
                    .inputItems(CustomTags.CIRCUITS_ARRAY[tier])
                    .inputFluids(SolderingAlloy, L)
                    .outputItems(CPU_FRAMES[tier])
                    .duration(200)
                    .EUt(VA[Math.max(tier, LV)])
                    .save(provider);
        }
        type.recipeBuilder(GTMQoL.id("me_data_center_interface"))
                .inputItems(GTMachines.HULL[IV].asStack())
                .inputItems(AEBlocks.INTERFACE.asItem())
                .inputItems(AEBlocks.CRAFTING_UNIT.asItem(), 4)
                .inputItems(AEItems.ENGINEERING_PROCESSOR.asItem(), 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(AE_INTERFACE)
                .duration(400)
                .EUt(VA[EV])
                .save(provider);
        // as the Large Assembler's, around AE2's crafting CPU blocks
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("me_data_center"), CONTROLLER.asStack(),
                "RSR", "CHC", "ASA",
                'C', CustomTags.IV_CIRCUITS,
                'R', GTItems.ROBOT_ARM_IV.asStack(),
                'S', AEBlocks.CRAFTING_STORAGE_256K.asItem(),
                'H', GTMachines.HULL[IV].asStack(),
                'A', AEBlocks.CRAFTING_ACCELERATOR.asItem());
    }

    private static IBlockPattern pattern(MultiblockMachineDefinition definition) {
        MultiPredicate edge = blocks("MachineCasings", CasingFrameBox.casingBlocks())
                .and(abilities(INTERFACE).setExactLimit(1).setPreviewCount(1));
        MultiPredicate inside = machines(CPU_FRAMES).setPreviewCount(1).and(air());
        return CasingFrameBox.box()
                .where('c', controller(definition))
                .where('e', edge)
                .where('g', blocks(AEBlocks.QUARTZ_GLASS.block()))
                .where('i', inside)
                .build();
    }
}
