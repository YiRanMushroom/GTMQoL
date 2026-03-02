package com.yiran.minecraft.gtmqol.init;

import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.yiran.minecraft.gtmqol.GTMQoL.LOGGER;

public class OverclockingPatcher {

    private static final UnsafeAdapter UNSAFE = UnsafeAdapter.create();

    public static void init() {
        try {
            LOGGER.info("Patching OverclockingLogic fields to enable sub-tick parallel and modify duration factors...");

            double newStdDuration = 0.25;
            double newPerfectDuration = 0.125;
            double stdVoltage = 4.0;

            setStaticFinalField(OverclockingLogic.class, "PERFECT_OVERCLOCK",
                    OverclockingLogic.create(newPerfectDuration, stdVoltage, true));

            setStaticFinalField(OverclockingLogic.class, "NON_PERFECT_OVERCLOCK",
                    OverclockingLogic.create(newStdDuration, stdVoltage, true));

            setStaticFinalField(OverclockingLogic.class, "PERFECT_OVERCLOCK_SUBTICK",
                    OverclockingLogic.create(newPerfectDuration, stdVoltage, true));

            setStaticFinalField(OverclockingLogic.class, "NON_PERFECT_OVERCLOCK_SUBTICK",
                    OverclockingLogic.create(newStdDuration, stdVoltage, true));

            LOGGER.info("Successfully patched OverclockingLogic fields!");
            LOGGER.info("New duration factors: STD={}, PERFECT={}", newStdDuration, newPerfectDuration);
        } catch (Exception e) {
            LOGGER.error("Failed to patch OverclockingLogic fields", e);
            throw new RuntimeException("Critical: OverclockingLogic patching failed", e);
        }
    }

    private static void setStaticFinalField(Class<?> clazz, String fieldName, Object newValue) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        UNSAFE.putStaticObject(field, newValue);
        LOGGER.info("Set {} = {}", fieldName, newValue);
    }

    private static final class UnsafeAdapter {
        private final Object unsafe;
        private final Method putObject;
        private final Method staticFieldOffset;
        private final Method staticFieldBase;

        private UnsafeAdapter(Object unsafe, Method putObject, Method staticFieldOffset, Method staticFieldBase) {
            this.unsafe = unsafe;
            this.putObject = putObject;
            this.staticFieldOffset = staticFieldOffset;
            this.staticFieldBase = staticFieldBase;
        }

        static UnsafeAdapter create() {
            UnsafeAdapter adapter = tryCreate("sun.misc.Unsafe");
            if (adapter != null) {
                return adapter;
            }
            adapter = tryCreate("jdk.internal.misc.Unsafe");
            if (adapter != null) {
                return adapter;
            }
            throw new RuntimeException("Failed to initialize Unsafe");
        }

        private static UnsafeAdapter tryCreate(String className) {
            try {
                Class<?> unsafeClass = Class.forName(className);
                Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                Object unsafe = theUnsafeField.get(null);

                Method putObject = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
                Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
                Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);

                return new UnsafeAdapter(unsafe, putObject, staticFieldOffset, staticFieldBase);
            } catch (Throwable ignored) {
                return null;
            }
        }

        void putStaticObject(Field field, Object newValue) throws Exception {
            Object base = staticFieldBase.invoke(unsafe, field);
            long offset = (long) staticFieldOffset.invoke(unsafe, field);
            putObject.invoke(unsafe, base, offset, newValue);
        }
    }
}
