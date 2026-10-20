package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MultiblockMachineBuilder;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.mui.GTSingleblockMachinePanels;
import com.yiran.minecraft.gtmqol.integration.ae2.AE2Machines;
import com.yiran.minecraft.gtmqol.integration.ae2.AEDualParts;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import com.yiran.minecraft.gtmqol.integration.ae2.StickyCardItem;
import com.yiran.minecraft.gtmqol.common.assembler.MagicalAssembler;
import com.yiran.minecraft.gtmqol.common.circuit.ControlCircuits;
import com.yiran.minecraft.gtmqol.common.crystal.CrystalGrowth;
import com.yiran.minecraft.gtmqol.common.ghost.GhostIngredients;
import com.yiran.minecraft.gtmqol.common.greenhouse.Greenhouse;
import com.yiran.minecraft.gtmqol.common.implosion.ElectricImplosion;
import com.yiran.minecraft.gtmqol.common.part.LegacyPartRecipes;
import com.yiran.minecraft.gtmqol.common.singleblock.ComposableTieredMachine;
import com.yiran.minecraft.gtmqol.common.singleblock.SingleblockFeature;
import com.yiran.minecraft.gtmqol.data.recipe.MiscRecipes;
import com.yiran.minecraft.gtmqol.common.steam.AdvancedSteamMachines;
import com.yiran.minecraft.gtmqol.data.recipe.EarlyGameRecipes;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMachineBuilder;
import com.yiran.minecraft.gtmqol.api.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.api.generation.RuntimeGeneration;
import com.yiran.minecraft.gtmqol.common.modular.ModularMachines;
import com.yiran.minecraft.gtmqol.gregification.Gregification;
import com.yiran.minecraft.gtmqol.common.multiblock.GTMQoLMultiblocks;
import com.yiran.minecraft.gtmqol.config.EarlyConfig;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.data.recipe.WirelessRecipes;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.VN;

@GTAddon(GTMQoL.MOD_ID)
public final class GTMQoLAddon implements IGTAddon {
    // Listeners are registered from the GTMQoL constructor, on our own mod bus.
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID, false);

    static {
        // Registrate's default tab is SEARCH: every item would add itself there through the tab contents event,
        // on top of the search tab already listing every tab's items, and NeoForge throws on the duplicate.
        // Same as gtceu's GTRegistration.
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private static final String MAIN_TAB_TITLE_KEY = "itemGroup." + GTMQoL.MOD_ID + ".main";

    // Same pattern as GTCreativeModeTabs. Everything registered through REGISTRATE after this line
    // (items, blocks, machine items) is tagged with this tab and listed by the display generator.
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN_TAB = REGISTRATE.defaultCreativeTab("main",
            builder -> builder.displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator("main", REGISTRATE))
                    .icon(() -> GTItems.TOOL_DATA_STICK.asStack())
                    .title(Component.translatable(MAIN_TAB_TITLE_KEY)))
            .register();

    static {
        REGISTRATE.creativeModeTab(MAIN_TAB);
        RuntimeGeneration.addLanguageEntry(GTMQoL.MOD_ID, "en_us", MAIN_TAB_TITLE_KEY, "GTM Quality of Life");
    }

    public static GTRegistrate registrate() {
        return REGISTRATE;
    }

    /**
     * Same as {@code registrate().machine(...)}, but returns our builder so {@code dynamicallyGenerated} can be
     * chained.
     */
    public static <M extends MetaMachine> GTMQoLMachineBuilder<MachineDefinition, M> machine(String name,
                                                                                         MachineInstanceFactory<M> factory) {
        return REGISTRATE.entry(name, callback -> new GTMQoLMachineBuilder<>(REGISTRATE, name, callback, factory));
    }

    /**
     * An electric single block like GT's ({@code lv_<name>} etc.): a {@link ComposableTieredMachine} with the given
     * features, GT's single block UI, facing sideways. Then set its recipe type with
     * {@link GTMQoLMachineBuilder#workableTiered}.
     */
    public static GTMQoLMachineBuilder<MachineDefinition, ComposableTieredMachine> tieredMachine(String name, int tier,
                                                                                               SingleblockFeature... features) {
        return machine(VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                info -> new ComposableTieredMachine(info, tier, features))
                .tier(tier)
                .ui(GTSingleblockMachinePanels.GENERAL_MACHINE)
                .rotationState(RotationState.NON_Y_AXIS);
    }

    /**
     * Same as {@code registrate().multiblock(...)}, see {@link #machine}.
     */
    public static <M extends MultiblockControllerMachine> GTMQoLMultiblockBuilder<M> multiblock(String name,
                                                                                            MachineInstanceFactory<M> factory) {
        // MultiblockMachineBuilder fixes its SELF type, which is what entry() infers; it is our builder.
        return (GTMQoLMultiblockBuilder<M>) REGISTRATE.<MachineDefinition, MultiblockMachineDefinition, GTRegistrate, MultiblockMachineBuilder<M>>entry(
                name, callback -> new GTMQoLMultiblockBuilder<>(REGISTRATE, name, callback, factory));
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    /** GTCEu generates these into its runtime data pack along with its own recipes. */
    @Override
    public void addRecipes(RecipeOutput provider) {
        GTMQoLConfig config = GTMQoLConfig.get();
        MagicalAssembler.addRecipes(provider);
        GhostIngredients.addRecipes(provider);
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
        if (config.recipes.useLegacyPartRecipes) LegacyPartRecipes.addRecipes(provider);
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
}
