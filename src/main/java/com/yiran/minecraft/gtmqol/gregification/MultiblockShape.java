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
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.Comparator;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

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
                    .where('S', controller(blocks(definition.getBlock())))
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
                    .where('S', controller(blocks(definition.getBlock())))
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
                    .where('S', controller(blocks(definition.getBlock())))
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
                    .where('S', controller(blocks(definition.getBlock())))
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
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_STEEL_SOLID.get()).setMinGlobalLimited(14)
                            .and(autoAbilities(definition.getRecipeTypes()))
                            .and(autoAbilities(true, true, false)))
                    .where('#', air())
                    .build();
        }
    },
    DISTILLATION_TOWER(CASING_STAINLESS_CLEAN, "block/casings/solid/machine_casing_clean_stainless_steel",
            "block/multiblock/distillation_tower") {

        @Override
        IBlockPattern pattern(MultiblockMachineDefinition definition) {
            MultiPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X);
            if (GTCEu.Mods.isAE2Loaded()) {
                exportPredicate = exportPredicate.xor(machines(GTAEMachines.FLUID_EXPORT_HATCH_ME));
            }
            exportPredicate = exportPredicate.setMaxLayerLimited(1);
            MultiPredicate maint = autoAbilities(true, false, false).setMaxGlobalLimited(1);
            return MultiblockPatternBuilder.start(UP, BACK, RIGHT)
                    .slice("YSY", "YYY", "YYY")
                    .slice("ZZZ", "Z#Z", "ZZZ")
                    .sliceRepeatable(0, 10, "XXX", "X#X", "XXX")
                    .slice("XXX", "XXX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
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

    final BlockEntry<Block> casing;
    final ResourceLocation casingModel;
    final ResourceLocation overlay;

    MultiblockShape(BlockEntry<Block> casing, String casingModel, String overlay) {
        this.casing = casing;
        this.casingModel = GTCEu.id(casingModel);
        this.overlay = GTCEu.id(overlay);
    }

    abstract IBlockPattern pattern(MultiblockMachineDefinition definition);

    MachineInstanceFactory<WorkableElectricMultiblockMachine> machine() {
        return WorkableElectricMultiblockMachine::new;
    }

    void configure(GTMQoLMultiblockBuilder<?> builder) {
        builder.rotationState(RotationState.ALL);
    }
}
