package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.client.MatrixPartRender;
import com.yiran.minecraft.gtmqol.common.crafter.PatternCrafter;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessFEMachines;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.integration.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SolderingAlloy;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;

/**
 * The Pattern Matrix: a hollow box that crafts AE2's and our crafting patterns in bulk for the ME network. Its edges are
 * machine casings of one tier, which gives its speed, with its interface, FE and catalyst parts among them; its faces
 * are quartz glass; pattern and processing cores fill the inside. Sized like a cleanroom, the controller in the middle
 * of the front's bottom edge. Only touch this class when AE2 is loaded.
 */
public final class PatternMatrix {

    /** The ME network side of the matrix; exactly one. */
    public static final PartAbility INTERFACE = new PartAbility("pattern_matrix_interface");
    /** Which casing tier the controller shows; before forming, the one of its item. */
    public static final IntegerProperty CASING_TIER = IntegerProperty.create("casing_tier", ULV, MAX);

    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 7;
    private static final int MIN_DEPTH = 2;
    private static final int MAX_DEPTH = 14;

    public static BlockEntry<ProcessingCoreBlock> PROCESSING_CORE;
    public static MachineEntry<MachineDefinition> PATTERN_CORE;
    public static MachineEntry<MachineDefinition> AE_INTERFACE;
    public static MachineEntry<MultiblockMachineDefinition> CONTROLLER;

    private PatternMatrix() {}

    public static void init() {
        PatternDetailsHelper.registerDecoder(CrafterPatternDetails.Decoder.INSTANCE);
        var registrate = GTMQoLAddon.registrate();
        PROCESSING_CORE = registrate.block("matrix_processing_core", ProcessingCoreBlock::new)
                .lang("Matrix Processing Core")
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        new ModelFile.UncheckedModelFile(GTMQoL.id("block/matrix_processing_core"))))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .item(BlockItem::new)
                .build()
                .register();
        PATTERN_CORE = GTMQoLAddon.machine("matrix_pattern_core", MatrixPatternCoreMachine::new)
                .rotationState(RotationState.NONE)
                .model(GTMachineModels.createBasicMachineModel(GTMQoL.id("block/matrix_pattern_core")))
                .langValue("Matrix Pattern Core")
                .tooltips(Component.translatable(PatternMatrixMachine.KEY + "pattern_core.tooltip"))
                .register();
        AE_INTERFACE = GTMQoLAddon.machine("matrix_me_interface", MatrixInterfaceMachine::new)
                .tier(LuV)
                .rotationState(RotationState.ALL)
                .abilities(INTERFACE)
                .modelProperty(IS_FORMED, false)
                .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
                .langValue("Matrix ME Interface")
                .tooltips(Component.translatable(PatternMatrixMachine.KEY + "interface.tooltip"))
                .register();
        CONTROLLER = GTMQoLAddon.multiblock("pattern_matrix", PatternMatrixMachine::new)
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                .appearanceBlock(GTBlocks.MACHINE_CASING_IV)
                .partAppearance((controller, part, side) -> ((PatternMatrixMachine) controller).casingState())
                .pattern(PatternMatrix::pattern)
                .allowExtendedFacing(false)
                .allowFlip(false)
                .modelProperty(CASING_TIER, IV)
                .modelProperty(RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                .model(controllerModel().andThen(b -> b.addDynamicRenderer(MatrixPartRender::new)))
                .langValue("Pattern Matrix")
                .tooltips(Component.translatable(PatternMatrixMachine.KEY + "tooltip.0"),
                        Component.translatable(PatternMatrixMachine.KEY + "tooltip.1"),
                        Component.translatable(PatternMatrixMachine.KEY + "tooltip.2"),
                        Component.translatable(PatternMatrixMachine.KEY + "tooltip.3"),
                        Component.translatable(PatternMatrixMachine.KEY + "tooltip.4"),
                        Component.translatable(PatternMatrixMachine.KEY + "tooltip.5"))
                .register();
        PatternMatrixMachine.addLang();
    }

    /** No block entity, the matrix only counts them. */
    public static final class ProcessingCoreBlock extends Block {

        public ProcessingCoreBlock(Properties properties) {
            super(properties);
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                    TooltipFlag flag) {
            tooltip.add(Component.translatable(PatternMatrixMachine.KEY + "processing_core.tooltip"));
        }
    }

    public static void addRecipes(RecipeOutput provider, boolean meAssembler) {
        GTRecipeTypeEntry type = meAssembler ? AEProcessing.ME_ASSEMBLER_RECIPES : GTRecipeTypes.ASSEMBLER_RECIPES;
        type.recipeBuilder(GTMQoL.id("matrix_pattern_core"))
                .inputItems(AEBlocks.PATTERN_PROVIDER.asItem(), 4)
                .inputItems(CustomTags.MV_CIRCUITS)
                .inputItems(AEItems.BLANK_PATTERN.asItem(), 8)
                .inputFluids(SolderingAlloy, L)
                .outputItems(PATTERN_CORE)
                .duration(200)
                .EUt(VA[MV])
                .save(provider);
        type.recipeBuilder(GTMQoL.id("matrix_processing_core"))
                .inputItems(AEBlocks.MOLECULAR_ASSEMBLER.asItem())
                .inputItems(AEItems.SPEED_CARD.asItem(), 5)
                .inputItems(AEBlocks.CRAFTING_ACCELERATOR.asItem(), 4)
                .inputFluids(SolderingAlloy, L)
                .outputItems(PROCESSING_CORE.asStack())
                .duration(200)
                .EUt(VA[MV])
                .save(provider);
        type.recipeBuilder(GTMQoL.id("matrix_me_interface"))
                .inputItems(GTMQoLConfig.get().ae2.overclockedPatternBuffer ? AE2Machines.OVERCLOCKED_ME_PATTERN_BUFFER :
                        GTAEMachines.ME_PATTERN_BUFFER)
                .inputItems(AEBlocks.INTERFACE.asItem())
                .inputItems(AEBlocks.PATTERN_PROVIDER.asItem())
                .inputItems(AEItems.ENGINEERING_PROCESSOR.asItem(), 4)
                .inputFluids(SolderingAlloy, L * 2)
                .outputItems(AE_INTERFACE)
                .duration(400)
                .EUt(VA[HV])
                .save(provider);
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("pattern_matrix"), CONTROLLER.asStack(),
                "CPC", "SHS", "RAR",
                'C', CustomTags.IV_CIRCUITS,
                'P', PatternCrafter.MACHINES[IV].asStack(),
                'S', AEBlocks.CRAFTING_STORAGE_64K.asItem(),
                'H', GTMachines.HULL[IV].asStack(),
                'R', GTItems.ROBOT_ARM_IV.asStack(),
                'A', AEBlocks.CRAFTING_ACCELERATOR.asItem());
    }

    /** The casing frame in every tier by index, ULV first. */
    static List<BlockEntry<Block>> casings() {
        return List.of(GTBlocks.MACHINE_CASING_ULV, GTBlocks.MACHINE_CASING_LV, GTBlocks.MACHINE_CASING_MV,
                GTBlocks.MACHINE_CASING_HV, GTBlocks.MACHINE_CASING_EV, GTBlocks.MACHINE_CASING_IV,
                GTBlocks.MACHINE_CASING_LuV, GTBlocks.MACHINE_CASING_ZPM, GTBlocks.MACHINE_CASING_UV,
                GTBlocks.MACHINE_CASING_UHV, GTBlocks.MACHINE_CASING_UEV, GTBlocks.MACHINE_CASING_UIV,
                GTBlocks.MACHINE_CASING_UXV, GTBlocks.MACHINE_CASING_OpV, GTBlocks.MACHINE_CASING_MAX);
    }

    /** The assembler's overlays on the casing of each tier. */
    private static MachineBuilder.ModelInitializer controllerModel() {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(GTCEu.id("block/machines/assembler"),
                    prov.getExistingFileHelper());
            builder.forAllStatesModelsExcept(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(GTMachineModels.SIDED_SIDED_OVERLAY_MODEL));
                GTMachineModels.tieredHullTextures(model, state.getValue(CASING_TIER));
                return GTMachineModels.addWorkableOverlays(overlays, state.getValue(RECIPE_LOGIC_STATUS), model);
            }, IS_FORMED);
        };
    }

    private static IBlockPattern pattern(MultiblockMachineDefinition definition) {
        Block[] casings = casings().stream().map(BlockEntry::get).toArray(Block[]::new);
        MultiPredicate edge = blocks("MachineCasings", casings)
                .and(abilities(INTERFACE).setExactLimit(1).setPreviewCount(1))
                .and(abilities(WirelessFEMachines.FE_INPUT).setMinGlobalLimited(1).setPreviewCount(1))
                .and(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1));
        MultiPredicate inside = blocks(PROCESSING_CORE.get()).setMinGlobalLimited(1).setPreviewCount(1)
                .and(machines(PATTERN_CORE).setMinGlobalLimited(1).setPreviewCount(1))
                .and(air());
        // as the cleanroom's: up, down, left, right, front, back of the controller
        return ExpandableMultiblockPatternBuilder
                .start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                .boundsProvider(bounds())
                .constraintProvider(() -> List.of(IntIntPair.of(MIN_DEPTH, MAX_DEPTH), IntIntPair.of(0, 0),
                        IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS),
                        IntIntPair.of(0, 0), IntIntPair.of(MIN_DEPTH, MAX_DEPTH)))
                .where('c', controller(definition))
                .where('e', edge)
                .where('g', blocks(AEBlocks.QUARTZ_GLASS.block()))
                .where('i', inside)
                .predicateProvider((pos, bounds) -> {
                    if (pos.equals(BlockPos.ZERO)) return 'c';
                    int faces = 0;
                    if (pos.getX() == bounds.get(0) || pos.getX() == -bounds.get(1)) faces++;
                    if (pos.getY() == -bounds.get(2) || pos.getY() == bounds.get(3)) faces++;
                    if (pos.getZ() == bounds.get(4) || pos.getZ() == -bounds.get(5)) faces++;
                    return faces >= 2 ? 'e' : faces == 1 ? 'g' : 'i';
                })
                .build();
    }

    /**
     * Follows the frame from the controller: along its edge to the left and right, then from the left corner back and
     * up. A frame can touch other casings, so its corners are told by the edge that turns off there.
     */
    static ExpandablePattern.BoundsProvider bounds() {
        return (level, controllerPos, front, up) -> {
            Direction left = front.getCounterClockWise();
            Direction back = front.getOpposite();
            BlockPos controller = controllerPos.immutable();
            int l = length(level, controller, left, back, MAX_RADIUS);
            int r = length(level, controller, left.getOpposite(), back, MAX_RADIUS);
            BlockPos corner = controller.relative(left, l);
            int b = length(level, corner, back, left.getOpposite(), MAX_DEPTH);
            int u = length(level, corner, up, left.getOpposite(), MAX_DEPTH);
            if (l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_DEPTH || u < MIN_DEPTH) {
                // the smallest that can work: two blocks inside, for the preview and auto-build
                return new IntArrayList(new int[] { MIN_DEPTH, 0, MIN_RADIUS, MIN_RADIUS, 0, MIN_DEPTH + 1 });
            }
            return new IntArrayList(new int[] { u, 0, l, r, 0, b });
        };
    }

    /** How far the edge runs along {@code along}; at its end one edge turns towards {@code turn}. */
    private static int length(Level level, BlockPos start, Direction along, Direction turn, int max) {
        for (int i = 1; i <= max; i++) {
            BlockPos pos = start.relative(along, i);
            if (!isFrame(level.getBlockState(pos))) return i - 1;
            if (isFrame(level.getBlockState(pos.relative(turn)))) return i;
        }
        return max;
    }

    private static boolean isFrame(BlockState state) {
        if (state.getBlock() instanceof MetaMachineBlock) return true;
        return casings().stream().anyMatch(casing -> casing.is(state.getBlock()));
    }
}
