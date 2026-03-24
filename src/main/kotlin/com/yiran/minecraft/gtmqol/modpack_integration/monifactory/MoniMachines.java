package com.yiran.minecraft.gtmqol.modpack_integration.monifactory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate;
import net.minecraft.network.chat.Component;
import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.common.block.MoniBlocks;
import net.neganote.monilabs.gtbridge.MoniRecipeTypes;

import java.awt.*;

public class MoniMachines {
//    public static MultiblockMachineDefinition PRISMATIC_CRUCIBLE = GTMQoLRegistrate.getREGISTRATE()
//            .multiblock("prismatic_crucible", WorkableElectricMultiblockMachine::new)
//            .rotationState(RotationState.NON_Y_AXIS)
//            .recipeTypes(MoniRecipeTypes.CHROMATIC_PROCESSING, MoniRecipeTypes.CHROMATIC_TRANSCENDENCE)
//            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
//            .appearanceBlock(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING)
//            .langValue("gtmqol.integration.monifactory.omni_prismatic_crucible")
//            .pattern(definition -> FactoryBlockPattern.start()
//                    // spotless:off
//                    .aisle("LLL#######LLL", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
//                    .aisle("LLLLL###LLLLL", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#l#####l#F#", "#lll#####lll#")
//                    .aisle("LLLLLLLLLLLLL", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##Fll###llF##", "#llll###llll#")
//                    .aisle("#LLCCCCCCCLL#", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "#llCCC#CCCll#", "#llll###llll#")
//                    .aisle("#LLCLLCLLCLL#", "#####LCL#####", "######C######", "#############", "#############", "#############", "#############", "#####lCl#####", "##lClCCClCl##", "##ll#####ll##")
//                    .aisle("##LCLLLLLCL##", "####L###L####", "#############", "#############", "#############", "#############", "######F######", "####llCll####", "###CCl#lCC###", "#############")
//                    .aisle("##LCCLLLCCL##", "####C###C####", "####C###C####", "#############", "#############", "#############", "#####FPF#####", "####CCCCC####", "####C###C####", "#############")
//                    .aisle("##LCLLLLLCL##", "####L###L####", "#############", "#############", "#############", "#############", "######F######", "####llCll####", "###CCl#lCC###", "#############")
//                    .aisle("#LLCLLCLLCLL#", "#####LCL#####", "######C######", "#############", "#############", "#############", "#############", "#####lCl#####", "##lClCCClCl##", "##ll#####ll##")
//                    .aisle("#LLCCCCCCCLL#", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "#llCCC#CCCll#", "#llll###llll#")
//                    .aisle("LLLLLLMLLLLLL", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##Fll###llF##", "#llll###llll#")
//                    .aisle("LLLLL###LLLLL", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#l#####l#F#", "#lll#####lll#")
//                    .aisle("LLL#######LLL", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
//                    // spotless:on
//                    .where("L",
//                            Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get())
//                                    .setMinGlobalLimited(88)
//                                    .or(Predicates.autoAbilities(definition.getRecipeTypes())))
//                    .where("l", Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get()))
//                    .where("C", Predicates.blocks(MoniBlocks.PRISM_GLASS.get()))
//                    .where("M", Predicates.controller(Predicates.blocks(definition.getBlock())))
//                    .where("P", Predicates.blocks(MoniBlocks.PRISMATIC_FOCUS.get()))
//                    .where("F", Predicates.frames(GTMaterials.Neutronium))
//                    .where("#", Predicates.any())
//                    .build())
//            .workableCasingModel(MoniLabs.id("block/casing/netherite"), GTCEu.id("block/multiblock/processing_array"))
//            .register();
}
