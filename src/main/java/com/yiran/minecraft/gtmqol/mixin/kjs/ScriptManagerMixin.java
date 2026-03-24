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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Mixin(value = ScriptManager.class, remap = false)
public abstract class ScriptManagerMixin {
    @Shadow @Final public ScriptType scriptType;

    @Shadow @Final public Map<String, ScriptPack> packs;

    @Shadow
    protected abstract void loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);

    @Inject(method = "loadFromDirectory", at = @At("TAIL"))
    private void gtmqol$injectJarScripts(CallbackInfo ci) {
        KJSInjector.init();
        if (KJSInjector.TASKS.isEmpty()) return;

        var pack = new ScriptPack((ScriptManager)(Object)this, new ScriptPackInfo("gtmqol", "kubejs/"));
        boolean added = false;

        for (var task : KJSInjector.TASKS) {
            if (task.type() != this.scriptType) continue;

            var fileInfo = new ScriptFileInfo(pack.info, task.kjsPath());
            ScriptSource jarSource = info -> {
                try (var is = KJSInjector.class.getResourceAsStream(task.jarPath())) {
                    if (is == null) return List.of();
                    try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        return reader.lines().toList();
                    }
                } catch (Exception e) {
                    return List.of();
                }
            };

            this.loadFile(pack, fileInfo, jarSource);
            added = true;
        }

        if (added) {
            this.packs.put(pack.info.namespace, pack);
            this.scriptType.console.info("[GTMQoL] Injected " + pack.scripts.size() + " scripts into " + this.scriptType.name);
        }
    }
}