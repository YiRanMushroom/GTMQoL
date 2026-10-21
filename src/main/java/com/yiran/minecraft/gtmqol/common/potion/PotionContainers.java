package com.yiran.minecraft.gtmqol.common.potion;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.core.mixins.PotionBrewingContainerMixesAccessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * Splash and lingering potion fluids: gtceu's potion fluid, marked with the potion item it bottles into (unmarked is
 * a plain potion). The brewery makes them with vanilla's container mixes (gunpowder, dragon's breath), glass bottles
 * fill into that item, and splash / lingering potions drain back into the marked fluid. The mark is an NBT string
 * next to gtceu's {@code Potion} key (1.21 uses a data component).
 */
public final class PotionContainers {

    private static final String CONTAINER_KEY = "gtmqol:potion_container";

    /** Recipe viewer hint on a potion input slot, which shows only gtceu's potion fluid. */
    public static final String ANY_MOD_KEY = "gtmqol.gui.content.any_mod_fluid";

    private PotionContainers() {}

    public static void init() {
        GTMQoLAddon.registrate().addRawLang(ANY_MOD_KEY, "Any mod's version works");
    }

    public static boolean isPotion(FluidStack fluid) {
        CompoundTag tag = fluid.getTag();
        return fluid.getFluid().defaultFluidState().is(CustomTags.POTION_FLUIDS) && tag != null && tag.contains("Potion", Tag.TAG_STRING);
    }

    /** The potion item {@code fluid} bottles into. */
    public static Item container(FluidStack fluid) {
        CompoundTag tag = fluid.getTag();
        if (tag == null || !tag.contains(CONTAINER_KEY, Tag.TAG_STRING)) return Items.POTION;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(CONTAINER_KEY));
        Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
        return item == Items.AIR ? Items.POTION : item;
    }

    /** A copy of {@code fluid} that bottles into {@code container}. */
    public static FluidStack withContainer(FluidStack fluid, Item container) {
        FluidStack copy = fluid.copy();
        if (container == Items.POTION) {
            copy.removeChildTag(CONTAINER_KEY);
        } else {
            copy.getOrCreateTag().putString(CONTAINER_KEY, BuiltInRegistries.ITEM.getKey(container).toString());
        }
        return copy;
    }

    /** Vanilla's container mixes. */
    public static List<PotionBrewing.Mix<Item>> mixes() {
        return PotionBrewingContainerMixesAccessor.gtmqol$getContainerMixes();
    }
}
