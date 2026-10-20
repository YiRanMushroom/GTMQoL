package com.yiran.minecraft.gtmqol.core.mixins;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Potion to splash potion, splash to lingering, for the brewery. */
@Mixin(PotionBrewing.class)
public interface PotionBrewingContainerMixesAccessor {

    @Accessor("CONTAINER_MIXES")
    static List<PotionBrewing.Mix<Item>> gtmqol$getContainerMixes() {
        throw new AssertionError();
    }
}
