package com.yiran.minecraft.gtmqol.data

import com.google.gson.JsonObject
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.common.data.models.GTMachineModels
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider
import com.lowdragmc.lowdraglib.Platform
import com.tterrag.registrate.providers.DataGenContext
import com.yiran.minecraft.gtmqol.logic.RegistryUtils.resourceLocationOf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

object ClientDynamicModelRegisterer {
    private val onClientSetupListeners: MutableList<() -> Unit> = mutableListOf()
    private val langBuffer: MutableMap<String, MutableMap<String, MutableMap<String, String>>> = mutableMapOf()

    @JvmStatic
    fun onGTCEuClientSetup(listener: () -> Unit) {
        if (!Platform.isClient()) {
            throw IllegalStateException("onClientSetup can only be called on client side")
        }
        onClientSetupListeners.add(listener)
    }

    @JvmStatic
    fun addLanguageEntry(namespace: String, lang: String, key: String, value: String) {
        val nsMap = langBuffer.getOrPut(namespace) { mutableMapOf() }
        val langMap = nsMap.getOrPut(lang) { mutableMapOf() }
        if (!langMap.containsKey(key)) {
            langMap[key] = value
        }
    }

    @JvmStatic
    fun registerItemModel(id: ResourceLocation, model: JsonObject) {
        GTDynamicResourcePack.addItemModel(id, model)
    }

    @JvmStatic
    fun setupClient() {
        onClientSetupListeners.forEach { it() }

        val provider = RuntimeBlockstateProvider.INSTANCE
        deferredAssetTasks.forEach { (builder, definition) ->
            val id = definition.id

            val blockModelPathStr = id.withPrefix("block/machine/").toString()

            val context = DataGenContext(definition::getBlock, blockModelPathStr, id)

            if (builder.blockModel() != null) {
                builder.blockModel()!!.accept(context, provider)
            } else if (builder.model() != null) {
                GTMachineModels.createMachineModel(builder.model()).accept(context, provider)
            }

            val itemModelName = id.toString()

            val parentBlockModel = resourceLocationOf(id.namespace, "block/machine/${id.path}")

            provider.itemModels().withExistingParent(itemModelName, parentBlockModel)

            provider.run()
        }
        deferredAssetTasks.clear()

        langBuffer.forEach { (namespace, langs) ->
            langs.forEach { (langCode, entries) ->
                val json = JsonObject()
                entries.forEach { (k, v) -> json.addProperty(k, v) }
                val langFileLoc = resourceLocationOf(namespace, "lang/$langCode.json")
                GTDynamicResourcePack.addResource(langFileLoc, json)
            }
        }
    }

    private val deferredAssetTasks: MutableList<Pair<MachineBuilder<*, *>, MachineDefinition>> = mutableListOf()

    @JvmStatic
    fun registerMachineAssets(builder: MachineBuilder<*, *>, definition: MachineDefinition) {
        if (!Platform.isClient()) return

        deferredAssetTasks.add(builder to definition)
    }

    fun <DEFINITION : MachineDefinition, TYPE : MachineBuilder<DEFINITION, TYPE>> MachineBuilder<DEFINITION, TYPE>
            .buildAndRegisterDynamicAssets(): DEFINITION {

        val definition = this.register()

        registerMachineAssets(this, definition)

        return definition
    }
}