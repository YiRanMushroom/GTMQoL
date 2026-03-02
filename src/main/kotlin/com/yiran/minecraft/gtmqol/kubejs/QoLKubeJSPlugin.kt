package com.yiran.minecraft.gtmqol.kubejs

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent

class QoLKubeJSPlugin : KubeJSPlugin() {
    override fun registerRecipeSchemas(event: RegisterRecipeSchemasEvent) {
        val qolNameSpace = event.namespace("gtmqol")

        qolNameSpace.register("greenhouse", GTRecipeSchema.SCHEMA)
    }
}