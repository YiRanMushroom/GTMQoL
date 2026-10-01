package com.yiran.minecraft.gtmqol;

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
import com.yiran.minecraft.gtmqol.generation.GTMQoLMachineBuilder;
import com.yiran.minecraft.gtmqol.generation.GTMQoLMultiblockBuilder;
import com.yiran.minecraft.gtmqol.generation.RuntimeGeneration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

@GTAddon
public final class GTMQoLAddon implements IGTAddon {
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID);

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

    @Override
    public String addonModId() {
        return GTMQoL.MOD_ID;
    }

}
