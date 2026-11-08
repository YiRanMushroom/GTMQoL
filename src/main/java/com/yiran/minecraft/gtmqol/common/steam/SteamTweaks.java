package com.yiran.minecraft.gtmqol.common.steam;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.yiran.minecraft.gtmqol.api.machine.MachineFeatures;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

/** GTCEu's steam machines made closer to the electric ones (config section {@code steamTweaks}). */
public final class SteamTweaks {

    private SteamTweaks() {}

    public static void init(IEventBus modBus) {
        GTMQoLConfig.SteamTweaks config = GTMQoLConfig.get().steamTweaks;
        if (config.noOutputLimits) {
            modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(SteamTweaks::removeOutputLimits));
        }
        if (config.circuitSlots || config.fluidTanks) {
            MachineFeatures.onCreated(SimpleSteamMachine.class, SteamTweaks::attachSingleblockTraits);
        }
        if (config.circuitSlots) {
            // GTCEu disables the ghost circuit slot on steam buses; the input bus gets it like the electric ones
            MachineFeatures.onCreated(SteamItemBusPartMachine.class,
                    bus -> bus.getCircuitSlot().setEnabled(bus.getInventory().getHandlerIO() == IO.IN));
        }
    }

    /**
     * The steam macerator, steam grinder and steam oven keep all of a recipe's outputs (byproducts) and get the slots
     * for them: GTCEu limits them to one item output. Their definitions' limit maps stay live after registration and
     * are read when a machine is built or runs, all after setup.
     */
    private static void removeOutputLimits() {
        for (MachineDefinition definition : List.of(GTMachines.STEAM_MACERATOR.left().get(),
                GTMachines.STEAM_MACERATOR.right().get(), GTMultiMachines.STEAM_GRINDER.get(), GTMultiMachines.STEAM_OVEN.get())) {
            definition.getRecipeOutputLimits().removeInt(ItemRecipeCapability.CAP);
        }
    }

    /**
     * Steam single blocks get the ghost circuit slot, attached the same way {@code SimpleTieredMachine} does; the steam
     * panel shows no trait configurators, {@code GTSingleblockMachinePanelsMixin} turns them on. They also get fluid
     * tanks sized by their recipe types, like {@code WorkableTieredMachine}. The input tank refuses steam, so steam only
     * goes to the steam tank. It has a higher trait priority than the steam tank (also an {@code IO.IN} fluid tank)
     * because the machine UI draws the first input tank as the recipe's fluid slots. The trait names are saved with the
     * machine: keep them.
     */
    private static void attachSingleblockTraits(SimpleSteamMachine machine) {
        GTMQoLConfig.SteamTweaks config = GTMQoLConfig.get().steamTweaks;
        if (config.circuitSlots) machine.attachPersistentTrait("circuit", new ProgrammableCircuitSlotTrait());
        if (!config.fluidTanks) return;
        var recipeTypes = machine.getRecipeTypes();
        int capacity = 8 * FluidType.BUCKET_VOLUME;
        NotifiableFluidTank importFluids = new NotifiableFluidTank(
                machine.getDefinition().getInputSize(FluidRecipeCapability.CAP, recipeTypes), capacity, IO.IN);
        importFluids.setFilter(f -> !f.is(GTMaterials.Steam.getFluidTag()));
        machine.attachPersistentTrait("gtmqol_import_fluids", importFluids, 2);
        machine.attachPersistentTrait("gtmqol_export_fluids", new NotifiableFluidTank(
                machine.getDefinition().getOutputSize(FluidRecipeCapability.CAP, recipeTypes), capacity, IO.OUT));
    }
}
