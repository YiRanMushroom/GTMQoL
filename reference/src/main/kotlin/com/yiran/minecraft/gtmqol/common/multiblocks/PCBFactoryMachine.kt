package com.yiran.minecraft.gtmqol.common.multiblocks

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.pattern.BlockPattern
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.pattern.Predicates
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.common.data.*
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine
import com.gregtechceu.gtceu.data.recipe.CustomTags
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.lowdragmc.lowdraglib.LDLib
import com.lowdragmc.lowdraglib.syncdata.ISubscription
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder
import com.yiran.minecraft.gtmqol.GTMQoLRegistrate
import com.yiran.minecraft.gtmqol.ModUtils
import com.yiran.minecraft.gtmqol.ModUtils.asNotNull
import com.yiran.minecraft.gtmqol.data.ClientDynamicModelRegisterer.buildAndRegisterDynamicAssets
import com.yiran.minecraft.gtmqol.data.QoLItems
import com.yiran.minecraft.gtmqol.data.QoLMultiblocks
import com.yiran.minecraft.gtmqol.data.QoLRecipeTypes
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.item.Item
import java.util.function.BiConsumer
import java.util.function.Consumer

class PCBFactoryMachine(holder: IMachineBlockEntity) :
    WorkableElectricMultiblockMachine(holder) {

    fun interface IPCBRecipeModifier {
        fun getModifier(count: Int): ModifierFunction

        companion object {
            @JvmStatic
            fun simpleModifier(outputMultiplier: Int, durationMultiplier: Int): IPCBRecipeModifier {
                return object : IPCBRecipeModifier {
                    override fun getModifier(count: Int): ModifierFunction {
                        if (count == prevCount) {
                            return prevModifier
                        }

                        return calculateModifier(count)
                    }

                    var prevCount: Int = 0
                    var prevModifier: ModifierFunction = ModifierFunction.IDENTITY

                    fun calculateModifier(count: Int): ModifierFunction {
                        prevCount = count

                        val adjustedOutputMultiplier = ModUtils.safePow(outputMultiplier, count, 65536).toInt()
                        val adjustedDurationMultiplier = ModUtils.safePow(durationMultiplier, count, 65536).toInt()

                        prevModifier =
                            ModifierFunction.builder().durationMultiplier(adjustedDurationMultiplier.toDouble())
                                .outputModifier(ContentModifier.multiplier(adjustedOutputMultiplier.toDouble())).build()

                        return prevModifier
                    }
                }
            }
        }
    }

    fun interface IPCBRecipeModifierProvider {
        fun providePCBRecipeModifier(): IPCBRecipeModifier
    }

    var inputBuses: List<ItemBusPartMachine> = emptyList()

    var currentModifier: ModifierFunction = ModifierFunction.IDENTITY

    @Persisted
    @DescSynced
    var modifierItem: Item? = null

    @Persisted
    @DescSynced
    var modifierCount: Int = 0

    var onChangedListeners = mutableListOf<ISubscription>()

    fun updateModifier() {
        val inputMultipliers = this.getInputMultipliers()
        if (inputMultipliers.size > 1) {
            this.modifierState = ModifierState.MULTIPLE_MODIFIERS.ordinal
            currentModifier = ModifierFunction.IDENTITY
            return
        }

        this.modifierState = ModifierState.NONE.ordinal

        if (inputMultipliers.isEmpty()) {
            this.currentModifier = ModifierFunction.IDENTITY
            return
        }

        val (provider, count) = inputMultipliers.entries.first()

        modifierItem = provider as? Item
        modifierCount = count
        currentModifier = provider.providePCBRecipeModifier().getModifier(count)
        modifierState = ModifierState.SINGLE_MODIFIER.ordinal
    }

    override fun onStructureFormed() {
        super.onStructureFormed()
        inputBuses = this.parts.mapNotNull { part ->
            if (part is ItemBusPartMachine && (part.inventory.handlerIO == IO.IN
                        || part.inventory.handlerIO == IO.BOTH)
            ) part else null
        }
        inputBuses.forEach {
            onChangedListeners.add(it.inventory.addChangedListener(::updateModifier))
        }
        updateModifier()
    }

    override fun onStructureInvalid() {
        modifierState = ModifierState.NONE.ordinal
        onChangedListeners.forEach { it.unsubscribe() }
        onChangedListeners.clear()
        inputBuses = emptyList()
        currentModifier = ModifierFunction.IDENTITY
        super.onStructureInvalid()
    }

    fun getInputMultipliers(): Map<IPCBRecipeModifierProvider, Int> {
        return inputBuses.flatMap { bus ->
            val slotCount = bus.inventory.slots
            (0 until slotCount).mapNotNull { slot ->
                val stack = bus.inventory.getStackInSlot(slot)
                if (!stack.isEmpty && stack.item is IPCBRecipeModifierProvider) Pair(
                    stack.item as IPCBRecipeModifierProvider,
                    stack.count
                ) else null
            }
        }.groupBy({ it.first }, { it.second }).mapValues { entry ->
            entry.value.sum()
        }
    }

    override fun createRecipeLogic(vararg args: Any?): RecipeLogic {
        return super.createRecipeLogic(*args)
    }

    enum class ModifierState {
        NONE,
        MULTIPLE_MODIFIERS,
        SINGLE_MODIFIER
    }

    @Persisted
    @DescSynced
    var modifierState: Int = 0

    object PCBFactoryMachineRecipeModifier : RecipeModifier {
        override fun getModifier(
            machine: MetaMachine,
            recipe: GTRecipe
        ): ModifierFunction {
            if (machine !is PCBFactoryMachine) {
                return RecipeModifier.nullWrongType(PCBFactoryMachineRecipeModifier::class.java, machine)
            }

            return machine.currentModifier
        }
    }

    override fun getFieldHolder(): ManagedFieldHolder {
        return MANAGED_FIELD_HOLDER
    }

    companion object {
        @JvmField
        val MANAGED_FIELD_HOLDER: ManagedFieldHolder = ManagedFieldHolder(
            PCBFactoryMachine::class.java,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER
        )

        fun additionalDisplay(controller: IMultiController, components: MutableList<Component>) {
            if (controller is PCBFactoryMachine) {
                if (controller.modifierState == ModifierState.MULTIPLE_MODIFIERS.ordinal) {
                    components.add(
                        Component.translatable("gtmqol.multiblock.pcb_factory_machine.error_multiple_modifiers")
                            .setStyle(
                                Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000))
                            )
                    )
                } else if (controller.modifierState == ModifierState.NONE.ordinal) {
                    components.add(Component.translatable("gtmqol.multiblock.pcb_factory_machine.no_modifiers"))
                } else if (controller.modifierState == ModifierState.SINGLE_MODIFIER.ordinal) {
                    components.add(
                        Component.translatable(
                            "gtmqol.multiblock.pcb_factory_machine.modifier_count",
                            controller.modifierCount,
                            Component.translatable(
                                controller.modifierItem?.descriptionId
                                    ?: "gtmqol.multiblock.pcb_factory_machine.unknown_modifier"
                            )
                        ).withStyle(
                            Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00))
                        )
                    )
                }
            }
        }

        fun getModifiers(): List<RecipeModifier> {
            return listOf<RecipeModifier>(
                PCBFactoryMachineRecipeModifier,
                GTRecipeModifiers.OC_NON_PERFECT_SUBTICK
            )
        }

        fun getPattern(definition: MultiblockMachineDefinition): BlockPattern {
            return FactoryBlockPattern.start()
                .aisle(
                    "ibbbi  clllc     ",
                    "ieeei  c   c     ",
                    "ieeei  c   c     ",
                    "ieeei  c   c     ",
                    "i   i  c   c     ",
                    "       c   c     ",
                    "       c   c     ",
                    "       c   c     ",
                    "       c   c     ",
                    "       clllc     "
                )
                .aisle(
                    "bbbbb  lllll     ",
                    "e   e   lll      ",
                    "e   e   lll      ",
                    "e   e   lll      ",
                    " bbb    lll      ",
                    "        lll      ",
                    "        lll      ",
                    "        lll      ",
                    "        lll      ",
                    "       lllll     "
                )
                .aisle(
                    "bbbbb  lllll     ",
                    "e   e   l l      ",
                    "e   e   l l      ",
                    "e   e   l l      ",
                    " bbb    l l      ",
                    "  b     l l      ",
                    "  b     l l      ",
                    "        l l      ",
                    "        l l      ",
                    "       lllll     "
                )
                .aisle(
                    "bbbbb  lllllg    ",
                    "e   e   lll      ",
                    "e   e   lll      ",
                    "e   e   lll      ",
                    " bbb    lll      ",
                    "        lll      ",
                    "  b     lll      ",
                    "        lll      ",
                    "        lll      ",
                    "       lllll     "
                )
                .aisle(
                    "ibbbi  clllcghddh",
                    "ieeei  c   c hddh",
                    "ieeei  c   c hddh",
                    "ieeei  c   c     ",
                    "i   i  c   c     ",
                    "       c   c     ",
                    "  b    c   c     ",
                    "       c   c     ",
                    "       c   c     ",
                    "       clllc     "
                )
                .aisle(
                    "            gdddd",
                    "             d  d",
                    "             d  d",
                    "             hddh",
                    "             hddh",
                    "                 ",
                    "  b              ",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "      ijjjjjidddd",
                    "      ijjjjjid  d",
                    "      ijjjjjid  d",
                    "      ijjjjjid  d",
                    "      i     id  d",
                    "             hddh",
                    "  b              ",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "ibbbi jkkkkkjdddd",
                    "ieeei j     jd  d",
                    "ieeei j     jd  d",
                    "ieeei j     jd  d",
                    "i   i jjjjjjjd  d",
                    "      i     idddd",
                    "  b          hddh",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "bbbbb jkkkkkjdddd",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    " bbb  jjjjjjjd  d",
                    "      i     idddd",
                    "  b          hddh",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "bbbbbgjkkkkkjdddd",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    " bbb  jjjjjjjd  d",
                    "  b   iiiiiiidddd",
                    "  b          hddh",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "bbbbb jkkkkkjdddd",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    "e   e j     jd  d",
                    " bbb  jeeeeejd  d",
                    "      i     idddd",
                    "             hddh",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "ibbbi jkkkkkjdddd",
                    "ieeei j     jd  d",
                    "ieeei j     jd  d",
                    "ieeei j     jd  d",
                    "i   i jeeeeejd  d",
                    "      i     idddd",
                    "             hddh",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "      ijjajjidddd",
                    "      ieeeeeid  d",
                    "      ieeeeeid  d",
                    "      ieeeeeid  d",
                    "      iiiiiiid  d",
                    "             hddh",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "             dddd",
                    "             d  d",
                    "             d  d",
                    "             hddh",
                    "             hddh",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .aisle(
                    "             hddh",
                    "             hddh",
                    "             hddh",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 ",
                    "                 "
                )
                .where("a", Predicates.controller(Predicates.blocks(definition.get())))
                .where(
                    "j", Predicates.blocks(GCYMBlocks.CASING_WATERTIGHT.get()).setMinGlobalLimited(50)
                        .or(Predicates.autoAbilities(*definition.recipeTypes))
                        .or(Predicates.autoAbilities(true, false, false))
                )
                .where("b", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                .where("c", Predicates.frames(GTMaterials.BlueSteel))
                .where("d", Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()))
                .where("e", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                .where("g", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where("h", Predicates.frames(GTMaterials.Ultimet))
                .where("i", Predicates.frames(GTMaterials.HSLASteel))
                .where("k", Predicates.blocks(GCYMBlocks.CASING_STRESS_PROOF.get()))
                .where("l", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                .where(" ", Predicates.any())
                .build()
        }

        @JvmStatic
        fun createDefinition(string: String): MultiblockMachineDefinition {
            return GTMQoLRegistrate.REGISTRATE.multiblock(string, ::PCBFactoryMachine)
                .rotationState(RotationState.NON_Y_AXIS)
                .allowExtendedFacing(false)
                .tooltips(Component.translatable("gtmqol.machine.pcb_factory.tooltip.0"))
                .tooltips(Component.translatable("gtmqol.machine.pcb_factory.tooltip.1"))
                .tooltips(Component.translatable("gtmqol.machine.pcb_factory.tooltip.2"))
                .tooltips(
                    Component.translatable(
                        "gtceu.machine.available_recipe_map_1.tooltip",
                        Component.translatable("gtceu.gtmqol_pcb_factory")
                    )
                )
                .recipeType(QoLRecipeTypes.PCB_FACTORY_RECIPES.asNotNull())
                .recipeModifiers(*getModifiers().toTypedArray())
                .appearanceBlock(GCYMBlocks.CASING_WATERTIGHT)
                .pattern(::getPattern)
                .additionalDisplay(BiConsumer(::additionalDisplay))
                .workableCasingModel(
                    GTCEu.id("block/casings/gcym/watertight_casing"),
                    GTCEu.id("block/multiblock/gcym/large_maceration_tower")
                )
                .buildAndRegisterDynamicAssets()
        }

        fun registerDefaultRecipes(provider: Consumer<FinishedRecipe>) {
            val PCBRecipeType = QoLRecipeTypes.PCB_FACTORY_RECIPES.asNotNull()

            PCBRecipeType.recipeBuilder("resin_printed_circuit_board")
                .inputItems(TagPrefix.plate, GTMaterials.Wood, 4)
                .inputItems(TagPrefix.foil, GTMaterials.Copper, 16)
                .inputFluids(GTMaterials.Glue, 400)
                .outputItems(GTItems.BASIC_CIRCUIT_BOARD, 16)
                .duration(10 * 20 * 4)
                .EUt(VA[LV].toLong())
                .circuitMeta(1)
                .save(provider)

            listOf(GTMaterials.Iron3Chloride, GTMaterials.SodiumPersulfate).forEach { material ->
                fun GTRecipeBuilder.applyFluidInput(material: Material, amount: Int): GTRecipeBuilder {
                    this.inputFluids(
                        material, if (material == GTMaterials.SodiumPersulfate)
                            amount * 2 else amount
                    )
                    return this
                }

                PCBRecipeType.recipeBuilder("phenolic_printed_circuit_board_from_${material.name}")
                    .inputItems(TagPrefix.dust, GTMaterials.Wood, 4)
                    .inputFluids(GTMaterials.Glue, 200)
                    .inputItems(TagPrefix.foil, GTMaterials.Copper, 16)
                    .applyFluidInput(material, 100)
                    .outputItems(GTItems.GOOD_CIRCUIT_BOARD, 16)
                    .duration(15 * 20 * 4)
                    .EUt(VA[LV].toLong())
                    .circuitMeta(1)
                    .save(provider)

                listOf(
                    GTMaterials.Polyethylene to 1, GTMaterials.PolyvinylChloride to 2,
                    GTMaterials.Polytetrafluoroethylene to 4, GTMaterials.Polybenzimidazole to 8
                ).forEach {
                    val (boardMaterial, outputMultiplier) = it
                    PCBRecipeType.recipeBuilder("plastic_printed_circuit_board_from_${boardMaterial.name}_and_${material.name}")
                        .inputItems(TagPrefix.plate, boardMaterial, 4)
                        .inputFluids(GTMaterials.SulfuricAcid, 1000)
                        .inputItems(TagPrefix.foil, GTMaterials.Copper, 16)
                        .inputItems(TagPrefix.foil, GTMaterials.Copper, 24)
                        .applyFluidInput(material, 1000)
                        .outputItems(GTItems.PLASTIC_CIRCUIT_BOARD, outputMultiplier * 16)
                        .duration(30 * 20 * 4)
                        .EUt(VA[LV].toLong())
                        .circuitMeta(1)
                        .save(provider)
                }

                PCBRecipeType.recipeBuilder("epoxy_printed_circuit_board_from_${material.name}")
                    .inputItems(TagPrefix.plate, GTMaterials.Epoxy, 4)
                    .inputFluids(GTMaterials.SulfuricAcid, 2000)
                    .inputItems(TagPrefix.foil, GTMaterials.Gold, 16)
                    .inputItems(TagPrefix.foil, GTMaterials.Electrum, 16)
                    .applyFluidInput(material, 2000)
                    .outputItems(GTItems.ADVANCED_CIRCUIT_BOARD, 16)
                    .duration(45 * 20 * 4)
                    .EUt(VA[LV].toLong())
                    .circuitMeta(1)
                    .save(provider)

                PCBRecipeType.recipeBuilder("fiber_reinforced_circuit_board_from_${material.name}")
                    .inputItems(TagPrefix.plate, GTMaterials.ReinforcedEpoxyResin, 4)
                    .inputFluids(GTMaterials.SulfuricAcid, 500)
                    .inputItems(TagPrefix.foil, GTMaterials.AnnealedCopper, 32)
                    .inputItems(TagPrefix.foil, GTMaterials.AnnealedCopper, 48)
                    .applyFluidInput(material, 4000)
                    .outputItems(GTItems.EXTREME_CIRCUIT_BOARD, 16)
                    .duration(60 * 20 * 4)
                    .EUt(VA[LV].toLong())
                    .circuitMeta(1)
                    .save(provider)

                PCBRecipeType.recipeBuilder("multilayer_fiber_reinforced_circuit_board_from_${material.name}")
                    .inputItems(TagPrefix.plate, GTMaterials.ReinforcedEpoxyResin, 8)
                    .inputItems(TagPrefix.foil, GTMaterials.AnnealedCopper, 64)
                    .inputFluids(GTMaterials.SulfuricAcid, 1000)
                    .inputItems(TagPrefix.foil, GTMaterials.Palladium, 32)
                    .inputFluids(GTMaterials.SulfuricAcid, 2000)
                    .inputItems(TagPrefix.foil, GTMaterials.Platinum, 32)
                    .applyFluidInput(material, 2000)
                    .outputItems(GTItems.ELITE_CIRCUIT_BOARD, 16)
                    .duration(75 * 20 * 4)
                    .EUt(VA[LV].toLong())
                    .circuitMeta(2)
                    .save(provider)
            }

            if (!LDLib.isModLoaded("monilabs")) {
                PCBRecipeType.recipeBuilder("wetware_circuit_board")
                    .inputItems(TagPrefix.plate, GTMaterials.ReinforcedEpoxyResin, 8)
                    .inputItems(TagPrefix.foil, GTMaterials.AnnealedCopper, 64)
                    .inputFluids(GTMaterials.SulfuricAcid, 1000)
                    .inputItems(TagPrefix.foil, GTMaterials.Palladium, 32)
                    .inputFluids(GTMaterials.SulfuricAcid, 2000)
                    .inputItems(TagPrefix.foil, GTMaterials.NiobiumTitanium, 4)
                    .inputFluids(GTMaterials.SterileGrowthMedium, 1000)
                    .outputItems(GTItems.WETWARE_CIRCUIT_BOARD, 16)
                    .duration(25 * 20 * 4)
                    .EUt(VA[LV].toLong())
                    .save(provider)
            }


            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("qol_titanium_nanite")
                .inputItems(TagPrefix.dust, GTMaterials.Titanium, 64)
                .inputItems(GTMachines.HULL[EV], 4)
                .inputItems(CustomTags.IV_CIRCUITS, 16)
                .inputItems(GTItems.ROBOT_ARM_EV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_EV, 4)
                .inputItems(GTItems.FIELD_GENERATOR_EV, 4)
                .inputItems(GTItems.SENSOR_EV, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 144 * 16)
                .circuitMeta(5)
                .outputItems(QoLItems.TITANIUM_NANITE.get())
                .duration(60 * 20)
                .EUt(VA[EV].toLong())
                .save(provider)

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("qol_osmiridium_nanite")
                .inputItems(TagPrefix.dust, GTMaterials.Osmiridium, 64)
                .inputItems(GTMachines.HULL[LuV], 4)
                .inputItems(CustomTags.ZPM_CIRCUITS, 16)
                .inputItems(GTItems.ROBOT_ARM_LuV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_LuV, 4)
                .inputItems(GTItems.FIELD_GENERATOR_LuV, 4)
                .inputItems(GTItems.SENSOR_LuV, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 144 * 16)
                .circuitMeta(5)
                .outputItems(QoLItems.OSMIRIDIUM_NANITE.get())
                .duration(60 * 20)
                .EUt(VA[LuV].toLong())
                .save(provider)

            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("qol_neutronium_nanite")
                .inputItems(TagPrefix.dust, GTMaterials.Neutronium, 64)
                .inputItems(GTMachines.HULL[UV], 4)
                .inputItems(CustomTags.UHV_CIRCUITS, 16)
                .inputItems(GTItems.ROBOT_ARM_UV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_UV, 4)
                .inputItems(GTItems.FIELD_GENERATOR_UV, 4)
                .inputItems(GTItems.SENSOR_UV, 4)
                .inputFluids(GTMaterials.SolderingAlloy, 144 * 16)
                .circuitMeta(5)
                .outputItems(QoLItems.NEUTRONIUM_NANITE.get())
                .duration(60 * 20)
                .EUt(VA[UV].toLong())
                .save(provider)

            VanillaRecipeHelper.addShapedRecipe(
                provider, true,
                "pcb_factory_machine",
                QoLMultiblocks.PCB_FACTORY.asNotNull().asStack(),
                "CRC", "AMP", "CHC", 'C', CustomTags.EV_CIRCUITS,
                'R', GTMultiMachines.LARGE_CHEMICAL_REACTOR.asStack(),
                'A', GTMachines.ASSEMBLER[HV].asStack(),
                'P', GTMachines.CIRCUIT_ASSEMBLER[HV].asStack(),
                'M', GTItems.FIELD_GENERATOR_MV.asStack(),
                'H', GTMachines.HULL[EV].asStack()
            )
        }
    }
}