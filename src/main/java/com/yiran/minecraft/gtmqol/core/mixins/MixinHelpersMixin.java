package com.yiran.minecraft.gtmqol.core.mixins;

import com.gregtechceu.gtceu.core.MixinHelpers;
import com.yiran.minecraft.gtmqol.data.tag.WireTags;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/** Our runtime item tags, added where GT adds its material tags while tags load. */
@Mixin(value = MixinHelpers.class, remap = false)
public class MixinHelpersMixin {

    @Inject(method = "generateGTDynamicTags", at = @At("TAIL"))
    private static void gtmqol$addTags(Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagMap,
                                       Registry<?> registry, CallbackInfo ci) {
        if (registry == BuiltInRegistries.ITEM) WireTags.addTo(tagMap);
    }
}
