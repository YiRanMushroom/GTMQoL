package com.yiran.minecraft.gtmqol.v8;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

@GTAddon
public final class GTMQoLV8Addon implements IGTAddon {
    private static final GTRegistrate REGISTRATE = GTRegistrate.create(GTMQoLV8.MOD_ID);

    public static GTRegistrate registrate() {
        return REGISTRATE;
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
    }

    @Override
    public String addonModId() {
        return GTMQoLV8.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        RuntimeDataGenerator.begin(provider);
    }
}
