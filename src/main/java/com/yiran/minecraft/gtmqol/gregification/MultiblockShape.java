package com.yiran.minecraft.gtmqol.gregification;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.client.CasingPartRender;
import com.yiran.minecraft.gtmqol.gregification.create.SequencedAssemblyMachine;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.generators.BlockModelBuilder;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.Comparator;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.RECIPE_LOGIC_STATUS;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.CUBE_ALL_SIDED_OVERLAY_MODEL;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.addWorkableOverlays;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;

/**
 * Structures of gregified multiblocks. The GT ones are copied from {@code GTMultiMachines} (the pattern functions
 * aren't reachable from the registered definitions), with the controller swapped for ours. The blast furnace's coils
 * only set its temperature, nothing else of GT's coil logic.
 */
public enum MultiblockShape {

    /** 3x3x3 of steel casings, hollow, like the implosion compressor. */
    GENERIC(CASING_STEEL_SOLID, "block/casings/solid/machine_casing_solid_steel",
            "block/multiblock/large_chemical_reactor") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "X#X", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .and(autoAbilities(definition.getRecipeTypes()))
                            .and(autoAbilities(true, false, false)))
                    .where('#', air())
                    .build();
        }
    },
    /**
     * {@link #GENERIC}, but taking every item and fluid part whatever the recipe type's slots: parts of other
     * capabilities (e.g. the universal ME parts and the pattern buffer for chemicals) carry those abilities.
     */
    ANY_PARTS(CASING_STEEL_SOLID, "block/casings/solid/machine_casing_solid_steel",
            "block/multiblock/large_chemical_reactor") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "X#X", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(10)
                            .and(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)
                                    .setPreviewCount(1))
                            .and(abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS,
                                    PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                            .and(autoAbilities(true, false, false)))
                    .where('#', air())
                    .build();
        }
    },
    ELECTRIC_BLAST_FURNACE(CASING_INVAR_HEATPROOF, "block/casings/solid/machine_casing_heatproof",
            "block/multiblock/electric_blast_furnace") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "CCC", "CCC", "XXX")
                    .slice("XXX", "C#C", "C#C", "XMX")
                    .slice("XSX", "CCC", "CCC", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(CASING_INVAR_HEATPROOF.get()).setMinGlobalLimited(9)
                            .and(autoAbilities(definition.getRecipeTypes()))
                            .and(autoAbilities(true, false, false)))
                    .where('M', abilities(PartAbility.MUFFLER))
                    .where('C', heatingCoils())
                    .where('#', air())
                    .build();
        }

        // knows its coil temperature, see GregificationModifiers.COIL_TEMPERATURE
        @Override
        MachineInstanceFactory<WorkableElectricMultiblockMachine> machine() {
            return CoilWorkableElectricMultiblockMachine::new;
        }
    },
    VACUUM_FREEZER(CASING_ALUMINIUM_FROSTPROOF, "block/casings/solid/machine_casing_frost_proof",
            "block/multiblock/vacuum_freezer") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "X#X", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(14)
                            .and(autoAbilities(definition.getRecipeTypes()))
                            .and(autoAbilities(true, false, false)))
                    .where('#', air())
                    .build();
        }
    },
    IMPLOSION_COMPRESSOR(CASING_STEEL_SOLID, "block/casings/solid/machine_casing_solid_steel",
            "block/multiblock/implosion_compressor") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "X#X", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .and(autoAbilities(definition.getRecipeTypes()))
                            .and(autoAbilities(true, true, false)))
                    .where('#', air())
                    .build();
        }
    },
    /**
     * 3x3x3 of Create's andesite casings, hollow, without a maintenance hatch: simple processing machines. Only use it
     * when Create is loaded; the casing is looked up when the structure is first checked.
     */
    CREATE(() -> BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "andesite_casing")),
            ResourceLocation.fromNamespaceAndPath("create", "block/andesite_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")) {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "X#X", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(definition))
                    .where('X', blocks(casing.get()).setMinGlobalLimited(14)
                            .and(autoAbilities(definition.getRecipeTypes())))
                    .where('#', air())
                    .build();
        }

        @Override
        MachineBuilder.ModelInitializer model() {
            return casingParts();
        }
    },
    /** 5x5x5 of Create's brass casings, hollow, without a maintenance hatch. Only use it when Create is loaded. */
    CREATE_BRASS(() -> BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "brass_casing")),
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"),
            GTCEu.id("block/multiblock/gcym/large_assembler")) {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            String[] full = { "XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX" };
            String[] hollow = { "XXXXX", "X###X", "X###X", "X###X", "XXXXX" };
            return MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice(full)
                    .slice(hollow)
                    .slice(hollow)
                    .slice(hollow)
                    .slice("XXXXX", "XXXXX", "XXSXX", "XXXXX", "XXXXX")
                    .where('S', controller(definition))
                    .where('X', blocks(casing.get()).setMinGlobalLimited(80)
                            .and(autoAbilities(definition.getRecipeTypes())))
                    .where('#', air())
                    .build();
        }

        @Override
        MachineBuilder.ModelInitializer model() {
            return casingParts();
        }
    },
    /**
     * Like GT's assembly line, 3 to 16 slices: brass casings at the bottom (with any item and fluid parts), vanilla
     * glass with a bronze gearbox casing between them, a steel casing between brass casings above that, and one brass
     * casing on top in the middle (with 1-2 energy hatches).
     * Only use it when Create is loaded.
     */
    CREATE_ASSEMBLY_LINE(() -> BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "brass_casing")),
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"),
            GTCEu.id("block/multiblock/assembly_line")) {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            // no separate last slice: gtceu repeats greedily without backtracking, so a last slice like the
            // repeated one would be looked for past the end
            return MultiblockPatternBuilder.start(RIGHT, UP, BACK)
                    .slice("III", "GTG", "SAC", "#Y#")
                    .sliceRepeatable(SequencedAssemblyMachine.MIN_LENGTH - 1, SequencedAssemblyMachine.MAX_LENGTH - 1,
                            "III", "GTG", "CAC", "#Y#")
                    .where('S', controller(definition))
                    // none required: e.g. most recipes take no fluid; the ghost hatch is an item input
                    .where('I', blocks(casing.get())
                            .and(abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                            .and(abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                            .and(abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1)))
                    .where('Y', blocks(casing.get())
                            .and(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)
                                    .setPreviewCount(1)))
                    .where('C', blocks(casing.get()))
                    .where('G', blocks(Blocks.GLASS))
                    .where('T', blocks(CASING_BRONZE_GEARBOX.get()))
                    .where('A', blocks(CASING_STEEL_SOLID.get()))
                    .where('#', any())
                    .build();
        }

        @Override
        MachineInstanceFactory<WorkableElectricMultiblockMachine> machine() {
            return SequencedAssemblyMachine::new;
        }

        @Override
        MachineBuilder.ModelInitializer model() {
            return casingParts();
        }
    },
    DISTILLATION_TOWER(CASING_STAINLESS_CLEAN, "block/casings/solid/machine_casing_clean_stainless_steel",
            "block/multiblock/distillation_tower") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            MultiPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
            if (GTCEu.Mods.isAE2Loaded()) {
                exportPredicate = exportPredicate.xor(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
            }
            exportPredicate = exportPredicate.setMaxLayerLimited(1);
            MultiPredicate maint = autoAbilities(true, false, false).setMaxGlobalLimited(1);
            return MultiblockPatternBuilder.start(UP, BACK, RIGHT)
                    .slice("YSY", "YYY", "YYY")
                    .slice("ZZZ", "Z#Z", "ZZZ")
                    .sliceRepeatable(0, 10, "XXX", "X#X", "XXX")
                    .slice("XXX", "XXX", "XXX")
                    .where('S', controller(definition))
                    .where('Y', blocks(CASING_STAINLESS_CLEAN.get())
                            .and(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .and(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .and(abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .and(maint))
                    .where('Z', blocks(CASING_STAINLESS_CLEAN.get()).and(exportPredicate).and(maint))
                    .where('X', blocks(CASING_STAINLESS_CLEAN.get()).and(exportPredicate))
                    .where('#', air())
                    .build();
        }

        @Override
        void configure(GTMQoLMultiblockBuilder<?> builder) {
            builder.rotationState(RotationState.NON_Y_AXIS)
                    .allowExtendedFacing(false)
                    .partSorter(Comparator.comparingInt(p -> p.getBlockPos().getY()));
        }
    };

    final Supplier<? extends Block> casing;
    final ResourceLocation casingModel;
    final ResourceLocation overlay;

    MultiblockShape(BlockEntry<Block> casing, String casingModel, String overlay) {
        this(casing, GTCEu.id(casingModel), GTCEu.id(overlay));
    }

    MultiblockShape(Supplier<? extends Block> casing, ResourceLocation casingModel, ResourceLocation overlay) {
        this.casing = casing;
        this.casingModel = casingModel;
        this.overlay = overlay;
    }

    abstract IBlockPattern pattern(MultiblockMachineDefinition definition);

    MachineInstanceFactory<WorkableElectricMultiblockMachine> machine() {
        return WorkableElectricMultiblockMachine::new;
    }

    void configure(GTMQoLMultiblockBuilder<?> builder) {
        builder.rotationState(RotationState.ALL);
    }

    /** The controller's model: gtceu's workable casing model, which also gives formed parts the casing texture. */
    MachineBuilder.ModelInitializer model() {
        return createWorkableCasingMachineModel(casingModel, overlay);
    }

    /**
     * For casings with their own connected textures (Create's): the controller and formed parts drawn as the casing
     * block itself (see {@link CasingPartRender}), so they join the casings around them. Like
     * {@link #model()}, but the controller's own casing faces are gtceu's see-through texture (machines render in the
     * cutout layer), under the usual overlays.
     */
    MachineBuilder.ModelInitializer casingParts() {
        MachineBuilder.ModelInitializer model = (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlay, prov.getExistingFileHelper());
            builder.forAllStatesModelsExcept(state -> {
                BlockModelBuilder base = prov.models().nested()
                        .parent(prov.models().getExistingFile(CUBE_ALL_SIDED_OVERLAY_MODEL))
                        .texture("all", GTCEu.id("block/void"))
                        .texture("particle", casingModel);
                return addWorkableOverlays(overlays, state.getValue(RECIPE_LOGIC_STATUS), base);
            }, IS_FORMED);
            builder.addTextureOverride("all", casingModel);
        };
        return model.andThen(builder -> builder.addDynamicRenderer(
                () -> new CasingPartRender(casing.get().defaultBlockState())));
    }
}
