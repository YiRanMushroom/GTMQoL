package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.core.mixins.PotionBrewingContainerMixesAccessor;
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Splash and lingering potion fluids: gtceu's potion fluid, marked with the potion item it bottles into (unmarked is
 * a plain potion). The brewery makes them with vanilla's container mixes (gunpowder, dragon's breath), glass bottles
 * fill into that item, and splash / lingering potions drain back into the marked fluid.
 */
public final class PotionContainers {

    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, GTMQoL.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Item>> CONTAINER = COMPONENTS
            .registerComponentType("potion_container", builder -> builder
                    .persistent(BuiltInRegistries.ITEM.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.ITEM)));

    /** Recipe viewer hint on a potion input slot, which shows only gtceu's potion fluid. */
    public static final String ANY_MOD_KEY = "gtmqol.gui.content.any_mod_fluid";

    private PotionContainers() {}

    public static void init(IEventBus modBus) {
        COMPONENTS.register(modBus);
        GTMQoLAddon.registrate().addRawLang(ANY_MOD_KEY, "Any mod's version works");
    }

    public static boolean isPotion(FluidStack fluid) {
        return fluid.is(CustomTags.POTION_FLUIDS) && fluid.has(DataComponents.POTION_CONTENTS);
    }

    /** The potion item {@code fluid} bottles into. */
    public static Item container(FluidStack fluid) {
        return fluid.getOrDefault(CONTAINER.get(), Items.POTION);
    }

    /** A copy of {@code fluid} that bottles into {@code container}. */
    public static FluidStack withContainer(FluidStack fluid, Item container) {
        FluidStack copy = fluid.copy();
        if (container == Items.POTION) {
            copy.remove(CONTAINER.get());
        } else {
            copy.set(CONTAINER.get(), container);
        }
        return copy;
    }

    /** Vanilla's container mixes, empty when the recipe viewer loads before the client has a level. */
    public static List<PotionBrewing.Mix<Item>> mixes() {
        MinecraftServer server = GTCEu.getMinecraftServer();
        Level level = server != null ? server.overworld() : GregificationClient.level();
        if (level == null) return List.of();
        return ((PotionBrewingContainerMixesAccessor) level.potionBrewing()).gtmqol$getContainerMixes();
    }
}
