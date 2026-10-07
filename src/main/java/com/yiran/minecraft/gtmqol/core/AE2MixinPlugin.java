package com.yiran.minecraft.gtmqol.core;

import com.yiran.minecraft.gtmqol.config.EarlyConfig;

import net.neoforged.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies {@code gtmqol.ae2.mixins.json} only when AE2 is installed, and the ExtendedAE mixin only when ExtendedAE
 * is too. Same loading mod list check as {@link EAPMixinPlugin}.
 */
public class AE2MixinPlugin implements IMixinConfigPlugin {

    private static final String ENCODING_MIXIN = "com.yiran.minecraft.gtmqol.core.mixins.ae2.EncodingHelperMixin";
    private static final String EXTENDEDAE_MIXIN = "com.yiran.minecraft.gtmqol.core.mixins.ae2.PartSpecialStorageBusMixin";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var mods = FMLLoader.getLoadingModList();
        if (mods.getModFileById("ae2") == null) return false;
        if (mixinClassName.equals(ENCODING_MIXIN)) return EarlyConfig.AE2_UNIVERSAL_CIRCUIT_ENCODING;
        // everything else is the sticky card
        if (!EarlyConfig.AE2_STICKY_CARD) return false;
        return !mixinClassName.equals(EXTENDEDAE_MIXIN) || mods.getModFileById("extendedae") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
