package com.yiran.minecraft.gtmqol.data.tag;

import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.providers.ProviderType;

/**
 * Makes GT circuits and Mekanism control circuits interchangeable, tier by tier: {@code c:circuits/basic}
 * includes {@code #gtceu:circuits/lv}, and {@code gtceu:circuits/lv} includes Mekanism's basic control circuit
 * (optional, so the tag still loads without Mekanism). Including each tag in the other would be a cycle,
 * which the tag loader rejects, hence one tag reference plus one item.
 * <p>
 * ULV pairs with infused alloy the same way, through {@code c:alloys/advanced} (Mekanism's
 * {@code mekanism:alloys/infused} includes that tag).
 */
public final class CircuitTags {

    private record Pair(TagKey<Item> gt, String commonTag, String mekanismItem) {}

    private static final Pair[] PAIRS = {
            new Pair(CustomTags.ULV_CIRCUITS, "alloys/advanced", "alloy_infused"),
            new Pair(CustomTags.LV_CIRCUITS, "circuits/basic", "basic_control_circuit"),
            new Pair(CustomTags.MV_CIRCUITS, "circuits/advanced", "advanced_control_circuit"),
            new Pair(CustomTags.HV_CIRCUITS, "circuits/elite", "elite_control_circuit"),
            new Pair(CustomTags.EV_CIRCUITS, "circuits/ultimate", "ultimate_control_circuit"),
    };

    private CircuitTags() {}

    public static void init() {
        GTMQoLAddon.registrate().addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
            for (Pair pair : PAIRS) {
                prov.addTag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", pair.commonTag)))
                        .addTag(pair.gt);
                prov.addTag(pair.gt)
                        .addOptional(ResourceLocation.fromNamespaceAndPath("mekanism", pair.mekanismItem));
            }
        });
    }
}
