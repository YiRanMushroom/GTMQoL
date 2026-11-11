package com.yiran.minecraft.gtmqol.common.test;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.neoforged.fml.loading.FMLEnvironment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.throwables.ClassMetadataNotFoundException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads every class our mixins target, so a mixin that no longer fits its target (after a GTCEu or ModularUI bump)
 * fails the data run instead of the game later. Mixins apply when their target loads, and a data run never loads many
 * targets (machine UIs, screens, recipe viewers): a handler whose arguments no longer match would only crash once a
 * player opens a machine. Loading doesn't initialize the classes. Targets of mods that aren't installed are skipped.
 * The targets are read from our mixin configs and the {@code @Mixin} annotations in the class files (mixin classes
 * themselves can't be loaded).
 */
public final class MixinTargetCheck {

    /** The configs listed in neoforge.mods.toml. */
    private static final List<String> CONFIGS = List.of("gtmqol.mixins.json", "gtmqol.recipedb.mixins.json", "gtmqol.jeifix.mixins.json", "gtmqol.ae2.mixins.json");
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";

    private MixinTargetCheck() {}

    public static void run() {
        ClassLoader loader = MixinTargetCheck.class.getClassLoader();
        Set<String> targets = new LinkedHashSet<>();
        for (String config : CONFIGS) {
            JsonObject json;
            try (var reader = new InputStreamReader(open(loader, config), StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            String pkg = json.get("package").getAsString().replace('.', '/');
            for (String side : FMLEnvironment.dist.isClient() ? List.of("mixins", "client") : List.of("mixins", "server")) {
                if (!json.has(side)) continue;
                for (JsonElement mixin : json.getAsJsonArray(side)) {
                    targets.addAll(targetsOf(open(loader, pkg + "/" + mixin.getAsString().replace('.', '/') + ".class")));
                }
            }
        }
        List<String> failures = new ArrayList<>();
        int loaded = 0;
        for (String target : targets) {
            try {
                Class.forName(target, false, loader);
                loaded++;
            } catch (Throwable e) {
                Throwable cause = e;
                while (cause.getCause() != null) cause = cause.getCause();
                // a mod that isn't installed, or a target building on one (another mod's integration classes)
                if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError ||
                        cause instanceof ClassMetadataNotFoundException) continue;
                failures.add(target + ": " + cause);
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Mixins that don't apply:\n" + String.join("\n", failures));
        }
        GTMQoL.LOGGER.info("Mixin targets loaded: {} of {}", loaded, targets.size());
    }

    private static InputStream open(ClassLoader loader, String path) {
        InputStream in = loader.getResourceAsStream(path);
        if (in == null) throw new IllegalStateException("Missing " + path);
        return in;
    }

    private static List<String> targetsOf(InputStream classFile) {
        List<String> targets = new ArrayList<>();
        try (classFile) {
            new ClassReader(classFile).accept(new ClassVisitor(Opcodes.ASM9) {

                @Override
                public @Nullable AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (!MIXIN.equals(descriptor)) return null;
                    return new AnnotationVisitor(Opcodes.ASM9) {

                        @Override
                        public AnnotationVisitor visitArray(String name) {
                            return new AnnotationVisitor(Opcodes.ASM9) {

                                @Override
                                public void visit(String unused, Object value) {
                                    // value = classes, targets = names of private classes
                                    if (value instanceof Type type) targets.add(type.getClassName());
                                    else if (value instanceof String target) targets.add(target.replace('/', '.'));
                                }
                            };
                        }
                    };
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return targets;
    }
}
