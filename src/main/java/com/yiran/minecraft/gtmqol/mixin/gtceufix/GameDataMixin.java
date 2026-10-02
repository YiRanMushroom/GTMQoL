package com.yiran.minecraft.gtmqol.mixin.gtceufix;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * gtceu 1.21 posts the vanilla recipe type registry a second time under the key {@code gtceu:recipe_type}, only
 * so KubeJS can register GT recipe types by that name. Mods that handle {@code RegisterEvent} by
 * {@code event.getRegistry()} (SuperMartijn642's Core Lib, CyclopsCore) see the same registry twice and throw
 * ({@code Adding duplicate value}, {@code Tried registering ... after its registration event}), which breaks
 * loading in large packs. Only gtceu and KubeJS get that duplicate event now. Remove once gtceu fixes it.
 */
@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

    /**
     * gtceu also moves its own registries and the vanilla recipe type registry in front of everything. NeoForge
     * posts ATTRIBUTE first, and CyclopsCore relies on that: it queues all its entries on the ATTRIBUTE event and
     * throws for registries whose event already passed (recipe types), which aborts the rest of the ATTRIBUTE
     * event and leaves e.g. {@code neoforge:swim_speed} unbound. Puts ATTRIBUTE back in front; gtceu's
     * registries still come before everything else.
     */
    @ModifyReturnValue(method = "getRegistrationOrder", at = @At("RETURN"))
    private static Set<ResourceLocation> gtmqol$attributeFirst(Set<ResourceLocation> order) {
        Set<ResourceLocation> reordered = new LinkedHashSet<>();
        reordered.add(Registries.ATTRIBUTE.location());
        reordered.addAll(order);
        return reordered;
    }

    @Unique
    private static final Set<String> gtmqol$DUPLICATE_RECIPE_TYPE_LISTENERS = Set.of("gtceu", "kubejs");

    // ModernFix @Redirects the same call (registry progress bar); WrapOperation chains on top of it.
    @WrapOperation(method = "postRegisterEvents",
                   at = @At(value = "INVOKE",
                            target = "Lnet/neoforged/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/neoforged/bus/api/Event;)V"))
    private static void gtmqol$onlyGTSeesDuplicateRecipeTypes(Event event, Operation<Void> original) {
        if (!(event instanceof RegisterEvent registerEvent) ||
                !registerEvent.getRegistryKey().equals(GTRegistries.Keys.RECIPE_TYPE)) {
            original.call(event);
            return;
        }
        // Same as ModLoader.postEventWrapContainerInModOrder, restricted to the mods above.
        for (EventPriority phase : EventPriority.values()) {
            ModList.get().forEachModInOrder(container -> {
                if (!gtmqol$DUPLICATE_RECIPE_TYPE_LISTENERS.contains(container.getModId())) return;
                ModLoadingContext.get().setActiveContainer(container);
                container.acceptEvent(phase, registerEvent);
                ModLoadingContext.get().setActiveContainer(null);
            });
        }
    }
}
