package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;

import net.minecraft.world.level.block.Block;

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

    public static void generateMachineAssets(MachineBuilder<?, ?, ?> builder, MachineDefinition definition) {
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
        provider.itemModels().withExistingParent(definition.getName(),
                definition.getId().withPrefix("block/machine/"));

        provider.run();
    }
}
