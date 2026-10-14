package com.yiran.minecraft.gtmqol.kubejs

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema
import com.yiran.minecraft.gtmqol.api.QoLRecipeModifiers
import com.yiran.minecraft.gtmqol.integration.monifactory.MoniRecipeTypesExtension
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent
import dev.latvian.mods.kubejs.script.BindingsEvent

class QoLKubeJSPlugin : KubeJSPlugin() {
    override fun registerRecipeSchemas(event: RegisterRecipeSchemasEvent) {
        val qolNameSpace = event.namespace("gtmqol")

        qolNameSpace.register("greenhouse", GTRecipeSchema.SCHEMA)
        qolNameSpace.register("me_assembler", GTRecipeSchema.SCHEMA)
        qolNameSpace.register("me_circuit_slicer", GTRecipeSchema.SCHEMA)
        qolNameSpace.register("magical_assembler", GTRecipeSchema.SCHEMA)
    }

    override fun registerBindings(event: BindingsEvent) {
        event.add("QoLRecipeModifiers", QoLRecipeModifiers::class.java)
    }
}