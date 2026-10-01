package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import com.tterrag.registrate.util.entry.RegistryEntry;

@GTAddon
public final class GTMQoLAddon implements IGTAddon {
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID);

    // English text is supplied at runtime, see GTMQoL.onRegisterDynamicResources.
    public static final String MAIN_TAB_TITLE_KEY = "itemGroup." + GTMQoL.MOD_ID + ".main";

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
    }

    public static GTRegistrate registrate() {
        return REGISTRATE;
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
