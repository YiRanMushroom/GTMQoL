package com.yiran.minecraft.gtmqol.integration;

import dev.latvian.mods.kubejs.script.ScriptType;
import java.util.ArrayList;
import java.util.List;

public class KJSInjector {
    public record ScriptTask(String jarPath, String kjsPath, ScriptType type) {}
    public static final List<ScriptTask> TASKS = new ArrayList<>();

    public static void injectStartupScript(String jarPath, String kjsName) {
        addJarSource(jarPath, "startup_scripts/" + kjsName, ScriptType.STARTUP);
    }

    public static void injectServerScript(String jarPath, String kjsName) {
        addJarSource(jarPath, "server_scripts/" + kjsName, ScriptType.SERVER);
    }

    public static void injectClientScript(String jarPath, String kjsName) {
        addJarSource(jarPath, "client_scripts/" + kjsName, ScriptType.CLIENT);
    }

    public static void addJarSource(String jarPath, String kjsPath, ScriptType type) {
        String path = jarPath.startsWith("/") ? jarPath : "/" + jarPath;
        TASKS.add(new ScriptTask(path, kjsPath, type));
    }

    public static void init() {
    }
}