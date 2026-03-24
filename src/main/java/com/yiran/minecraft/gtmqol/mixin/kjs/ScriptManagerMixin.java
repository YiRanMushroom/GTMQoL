package com.yiran.minecraft.gtmqol.mixin.kjs;

import com.yiran.minecraft.gtmqol.integration.KJSInjector;
import dev.latvian.mods.kubejs.script.*;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(value = ScriptManager.class, remap = false)
public abstract class ScriptManagerMixin {
    @Final
    @Shadow
    public Map<String, ScriptPack> packs;

    @Shadow
    protected abstract void loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);

    @Inject(method = "loadFromResources", at = @At("TAIL"))
    private void gtmqol$injectJarScripts(ResourceManager resourceManager, CallbackInfo ci) {
        KJSInjector.init(KJSInjector.TASKS);

        if (KJSInjector.TASKS.isEmpty()) return;

        var pack = new ScriptPack((ScriptManager)(Object)this, new ScriptPackInfo("gtmqol", "kubejs/"));

        for (var task : KJSInjector.TASKS) {
            var fileInfo = new ScriptFileInfo(pack.info, task.kjsPath());

            ScriptSource jarSource = info -> {
                try (var is = KJSInjector.class.getResourceAsStream(task.jarPath())) {
                    if (is == null) return List.of();

                    try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        return reader.lines().toList();
                    }
                } catch (IOException e) {
                    return List.of();
                }
            };

            this.loadFile(pack, fileInfo, jarSource);
        }

        this.packs.put(pack.info.namespace, pack);
    }
}