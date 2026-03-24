package com.yiran.minecraft.gtmqol.integration;

import java.util.ArrayList;
import java.util.List;

public class KJSInjector {
    public record ScriptTask(String jarPath, String kjsPath) {}
    public static final List<ScriptTask> TASKS = new ArrayList<>();

    public static void injectStartUpScript(String fileName) {
        addJarSource("kjs/modpack_integration/" + fileName, "startup_scripts/" + fileName);
    }

    public static void injectServerScript(String fileName) {
        addJarSource("kjs/modpack_integration/" + fileName, "server_scripts/" + fileName);
    }

    public static void addJarSource(String jarPath, String kjsPath) {
        String path = jarPath.startsWith("/") ? jarPath : "/" + jarPath;
        TASKS.add(new ScriptTask(path, kjsPath));
    }

    public static void init(List<ScriptTask> tasks) {
        // Mixin initialization here, tasks is just KJSInjector.TASKS
    }
}