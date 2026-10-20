package com.yiran.minecraft.gtmqol.core;

import net.neoforged.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Base of our mixin config plugins: they only decide {@link #shouldApplyMixin}, so every other
 * {@link IMixinConfigPlugin} method is a no-op here.
 */
public abstract class GTMQoLMixinConfigPlugin implements IMixinConfigPlugin {

    /**
     * Whether a mod is installed. Mods aren't constructed yet when mixins apply, so this asks the loading mod list
     * (same check as GTCEu's {@code GTCEu.isModLoaded} before ModList exists).
     */
    protected static boolean isModLoading(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {}

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
