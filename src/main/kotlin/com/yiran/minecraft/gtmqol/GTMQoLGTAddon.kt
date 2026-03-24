package com.yiran.minecraft.gtmqol

import com.gregtechceu.gtceu.api.addon.GTAddon
import com.gregtechceu.gtceu.api.addon.IGTAddon
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.yiran.minecraft.gtmqol.data.QoLRecipes
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.Consumer

@GTAddon
class GTMQoLGTAddon : IGTAddon {
    override fun getRegistrate(): GTRegistrate {
        return GTMQoLRegistrate.REGISTRATE
    }

    override fun initializeAddon() {

    }

    override fun addonModId(): String {
        return GTMQoL.MOD_ID
    }

    override fun addRecipes(provider: Consumer<FinishedRecipe>) {
        QoLRecipes.init(provider)
    }

    override fun requiresHighTier(): Boolean {
        return true
    }

}