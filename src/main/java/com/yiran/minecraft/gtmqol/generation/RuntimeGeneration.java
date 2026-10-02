package com.yiran.minecraft.gtmqol.generation;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.gregtechceu.gtceu.utils.data.RuntimeBlockstateProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects client-side resources while content is registered, and publishes them all to GTCEu's
 * dynamic resource pack in {@link #generateAll()}.
 *
 * <p>Registrate only writes models and lang during Gradle data generation, and GTCEu only flushes the
 * model builders for its own content (and for KubeJS). Machines opt in with {@code dynamicGenerated(true)}
 * on their builder. Nothing is cleared after {@link #generateAll()}: GTCEu fires its event on every
 * resource reload, and the whole queue is replayed each time.</p>
 */
public final class RuntimeGeneration {

    private record MachineAssets(MachineBuilder<?, ?, ?> builder, MachineDefinition definition) {}

    private static final List<MachineAssets> MACHINES = new ArrayList<>();
    private static final List<Runnable> CALLBACKS = new ArrayList<>();
    // namespace -> language -> key -> value
    private static final Map<String, Map<String, Map<String, String>>> LANG = new LinkedHashMap<>();

    private RuntimeGeneration() {}

    /**
     * Called from {@code MachineBuilderMixin} when a builder marked as dynamically generated registers.
     */
    public static void addMachine(MachineBuilder<?, ?, ?> builder, MachineDefinition definition) {
        MACHINES.add(new MachineAssets(builder, definition));
    }

    /**
     * First value for a key wins.
     */
    public static void addLanguageEntry(String namespace, String language, String key, String value) {
        LANG.computeIfAbsent(namespace, ns -> new LinkedHashMap<>())
                .computeIfAbsent(language, l -> new LinkedHashMap<>())
                .putIfAbsent(key, value);
    }

    /**
     * For anything not covered above. Runs at the start of every {@link #generateAll()}, so it may call
     * the methods above or write to {@link GTDynamicResourcePack} directly.
     */
    public static void onGenerate(Runnable callback) {
        CALLBACKS.add(callback);
    }

    /**
     * Call from GTCEu's {@code RegisterDynamicResourcesEvent}.
     */
    public static void generateAll() {
        CALLBACKS.forEach(Runnable::run);

        RuntimeBlockstateProvider provider = RuntimeBlockstateProvider.INSTANCE;
        for (MachineAssets machine : MACHINES) {
            generateMachine(provider, machine.builder(), machine.definition());
        }
        provider.run();

        LANG.forEach((namespace, languages) -> languages.forEach((language, entries) -> {
            JsonObject json = new JsonObject();
            entries.forEach(json::addProperty);
            // One file per namespace and language: a second write would replace the first.
            GTDynamicResourcePack.addResource(ResourceLocation.fromNamespaceAndPath(namespace, "lang/" + language + ".json"), json);
        }));
    }

    private static void generateMachine(RuntimeBlockstateProvider provider, MachineBuilder<?, ?, ?> builder,
                                        MachineDefinition definition) {
        DataGenContext<Block, ? extends Block> context = new DataGenContext<>(
                definition::getBlock, definition.getName(), definition.getId());

        // Same dispatch GTCEu uses in MachineBuilder.KJSCallWrapper#generateAssetJsons for a null
        // generator. We cannot call that method because its signature needs a KubeJS type.
        if (builder.blockModel() != null) {
            builder.blockModel().accept(context, provider);
        } else if (builder.model() != null) {
            GTMachineModels.createMachineModel(builder.model()).accept(context, provider);
        }

        // The provider belongs to GTCEu's registrate, so a bare name would land in the gtceu namespace;
        // pass the full id.
        provider.itemModels().withExistingParent(definition.getId().toString(),
                definition.getId().withPrefix("block/machine/"));

        if (definition.getLangValue() != null) {
            addLanguageEntry(definition.getId().getNamespace(), "en_us", definition.getDescriptionId(),
                    definition.getLangValue());
        }
    }
}
