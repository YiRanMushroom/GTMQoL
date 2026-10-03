package com.yiran.minecraft.gtmqol.core;

import net.minecraftforge.fml.loading.FMLLoader;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies {@code gtmqol.eap.mixins.json} only when ExtendedAE Plus is installed. Mods aren't constructed yet when
 * mixins apply, so this asks the loading mod list (same check as GTCEu's {@code GTCEu.isModLoaded} before
 * ModList exists).
 */
public class EAPMixinPlugin implements IMixinConfigPlugin {

    private static final String EAP_MOD_ID = "extendedae_plus";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return FMLLoader.getLoadingModList().getModFileById(EAP_MOD_ID) != null;
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
