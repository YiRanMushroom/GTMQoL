package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.yiran.minecraft.gtmqol.config.GTMQoLConfig;
import com.yiran.minecraft.gtmqol.integration.IntegrationTests;

@GTAddon
public final class GTMQoLAddon implements IGTAddon {
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoL.MOD_ID);

    public static GTRegistrate registrate() {
        return REGISTRATE;
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        // GTRegistrate.create already hooked our mod event bus, so there is nothing else to register.
        if (GTMQoLConfig.INSTANCE.integrationTests.enabled) {
            IntegrationTests.registerExampleMachines();
        }
    }

    @Override
    public String addonModId() {
        return GTMQoL.MOD_ID;
    }

}
