package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.yiran.minecraft.gtmqol.integration.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.integration.ae2.AEDualParts;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.circuit.ControlCircuits;
import com.yiran.minecraft.gtmqol.common.crystal.CrystalGrowth;
import com.yiran.minecraft.gtmqol.common.greenhouse.Greenhouse;
import com.yiran.minecraft.gtmqol.common.implosion.ElectricImplosion;
import com.yiran.minecraft.gtmqol.data.recipe.MiscRecipes;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.data.recipe.EarlyGameRecipes;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMachineBuilder;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.common.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.config.EarlyConfig;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.data.recipe.WirelessRecipes;
import com.yiran.minecraft.gtmqol.gregification.Gregification;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Consumer;

@GTAddon
public final class GTMQoLAddon implements IGTAddon {
    // GTCEu instantiates addons while constructing itself, so this runs under GTCEu's mod loading context.
    // Registrate hooks GatherDataEvent onto FMLJavaModLoadingContext.get()'s bus, i.e. GTCEu's, and datagen
    // for gtmqol produces nothing. Listeners are registered from the GTMQoL constructor instead.
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID, false);

    private static final String MAIN_TAB_TITLE_KEY = "itemGroup." + GTMQoL.MOD_ID + ".main";

    // Same pattern as GTCreativeModeTabs. Everything registered through REGISTRATE after this line
    // (items, blocks, machine items) is tagged with this tab and listed by the display generator.
    public static final RegistryEntry<CreativeModeTab> MAIN_TAB = REGISTRATE.defaultCreativeTab("main",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", REGISTRATE))
                    .icon(() -> GTItems.TOOL_DATA_STICK.asStack())
                    .title(Component.translatable(MAIN_TAB_TITLE_KEY))
                    .build())
            .register();

    static {
        REGISTRATE.creativeModeTab(MAIN_TAB);
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", MAIN_TAB_TITLE_KEY, "GTM Quality of Life");
    }

    public static GTRegistrate registrate() {
        return REGISTRATE;
    }

    /**
     * Same as {@code registrate().machine(...)}, but returns our builder so {@code dynamicGenerated} can be
     * chained.
     */
    public static <M extends MetaMachine> GTMQoLMachineBuilder<MachineDefinition, M> machine(String name,
                                                                                         MachineInstanceFactory<M> factory) {
        return new GTMQoLMachineBuilder<>(REGISTRATE, name, MachineDefinition::new,
                MetaMachineBlock::new, MetaMachineItem::new, factory);
    }

    /**
     * Same as {@code registrate().multiblock(...)}, see {@link #machine}.
     */
    public static <M extends MultiblockControllerMachine> GTMQoLMultiblockBuilder<M> multiblock(String name,
                                                                                            MachineInstanceFactory<M> factory) {
        return new GTMQoLMultiblockBuilder<>(REGISTRATE, name, MetaMachineBlock::new, MetaMachineItem::new,
                factory);
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        // Too late for machines (registry already frozen), see GTMQoL.onRegisterMachines.
    }

    /** GTCEu generates these into its runtime data pack along with its own recipes. */
    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GTMQoLConfig config = GTMQoLConfig.get();
        MagicalAssembler.addRecipes(provider);
        if (config.wireless.energy) WirelessRecipes.addEnergyRecipes(provider);
        if (config.wireless.steam) WirelessRecipes.addSteamRecipes(provider);
        ModularMachines.addRecipes(provider);
        Gregification.addRecipes(provider);
        GTMQoLMultiblocks.addRecipes(provider);
        if (config.machines.advancedSteamMachines) AdvancedSteamMachines.addRecipes(provider);
        if (config.circuits.controlCircuits) ControlCircuits.addRecipes(provider);
        if (config.recipes.miscRecipes) MiscRecipes.addRecipes(provider);
        if (config.recipes.earlyGame) EarlyGameRecipes.addRecipes(provider);
        if (config.recipes.netherStarDust) MiscRecipes.addNetherStarDust(provider);
        if (config.machines.electricImplosionCompressor) ElectricImplosion.addRecipes(provider);
        if (config.machines.crystalGrowthChamber) CrystalGrowth.addRecipes(provider);
        if (config.machines.greenhouse) Greenhouse.addRecipes(provider);
        if (GTCEu.Mods.isAE2Loaded()) {
            if (config.ae2.overclockedPatternBuffer) AE2Machines.addRecipes(provider);
            if (config.ae2.processing) AEProcessing.addRecipes(provider);
            if (config.ae2.dualHatches) AEDualParts.addRecipes(provider, config.ae2.processing);
            if (EarlyConfig.AE2_STICKY_CARD) StickyCardItem.addRecipes(provider, config.ae2.processing);
        }
    }

    @Override
    public String addonModId() {
        return GTMQoL.MOD_ID;
    }

}
