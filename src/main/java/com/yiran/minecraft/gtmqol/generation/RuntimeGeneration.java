package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;
import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;

/**
 * Publishes the client-side JSON for machines we register, without Gradle data generation.
 *
 * <p>Registration itself happens normally through {@link MachineBuilder#register()}. GTCEu only
 * flushes the model builders for its own content (and for KubeJS addons); a plain Java addon has to
 * drive {@link RuntimeBlockstateProvider} itself.</p>
 */
public final class RuntimeGeneration {

    private RuntimeGeneration() {}

    public static void generateMachineAssets(MachineBuilder<?, ?, ?> builder, MachineDefinition definition,
                                             JsonObject lang) {
        RuntimeBlockstateProvider provider = RuntimeBlockstateProvider.INSTANCE;
        DataGenContext<Block, ? extends Block> context = new DataGenContext<>(
                definition::getBlock, definition.getName(), definition.getId());

        // Same dispatch GTCEu uses in MachineBuilder.KJSCallWrapper#generateAssetJsons for a null
        // generator. We cannot call that method because its signature needs a KubeJS type.
        if (builder.blockModel() != null) {
            builder.blockModel().accept(context, provider);
        } else if (builder.model() != null) {
            GTMachineModels.createMachineModel(builder.model()).accept(context, provider);
        }

        // Registrate only writes the item model during data generation, so publish it here as well.
        // The provider belongs to GTCEu's registrate, so a bare name would land in the gtceu namespace;
        // pass the full id.
        provider.itemModels().withExistingParent(definition.getId().toString(),
                definition.getId().withPrefix("block/machine/"));

        provider.run();

        // langValue is likewise only written by Registrate's lang datagen.
        if (definition.getLangValue() != null) {
            lang.addProperty(definition.getDescriptionId(), definition.getLangValue());
        }
    }

    /**
     * One file per language: a later call for the same language replaces the earlier one.
     */
    public static void addLanguage(String language, JsonObject entries) {
        GTDynamicResourcePack.addResource(new ResourceLocation(GTMQoL.MOD_ID, "lang/" + language + ".json"),
                entries);
    }
}
