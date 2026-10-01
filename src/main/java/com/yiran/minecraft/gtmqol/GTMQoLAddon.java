package com.yiran.minecraft.gtmqol;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

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
        REGISTRATE.registerRegistrate();
    }

    @Override
    public String addonModId() {
        return GTMQoL.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        RuntimeGeneration.beginRecipes(provider);
    }
}
