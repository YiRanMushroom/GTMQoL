package com.yiran.minecraft.gtmqol.api.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import brachy.modularui.value.sync.PanelSyncManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * What we add to machines we don't own (GTCEu's and other addons'), declared in one place instead of a mixin per
 * machine class. Each feature names the machines it is for and what it does to one, at a stage of the machine's life:
 * <ul>
 * <li>{@link #onCreated}: right after the machine is built, on both sides, before its saved data is read, which is
 * where persistent traits have to be attached; {@link #onRecipeType} for every machine running a recipe type;</li>
 * <li>{@link #onPanel}: when its UI panel has been built, also for panels that show no trait configurators.</li>
 * </ul>
 * Two mixins call in: {@code MachineBuilderMixin} wraps every machine builder's factory, and
 * {@code MachineUIPanelBuilderMixin} the panel builder.
 */
// Untested: steam single blocks still get the circuit slot and extra tanks and keep them after a reload, the steam
// input bus its circuit slot, every multiblock controller the auto-build button
public final class MachineFeatures {

    /** Adds to a machine's UI panel. */
    @FunctionalInterface
    public interface PanelFeature<T extends MetaMachine> {

        void addTo(T machine, MachineUIPanel panel, PanelSyncManager syncManager);
    }

    private record Created<T extends MetaMachine>(Class<T> type, Consumer<T> feature) {

        void apply(MetaMachine machine) {
            if (type.isInstance(machine)) feature.accept(type.cast(machine));
        }
    }

    private record Panel<T extends MetaMachine>(Class<T> type, PanelFeature<T> feature) {

        void apply(MetaMachine machine, MachineUIPanel panel, PanelSyncManager syncManager) {
            if (type.isInstance(machine)) feature.addTo(type.cast(machine), panel, syncManager);
        }
    }

    /** The factory a machine builder ends up with: it builds the machine, then gives it its features. */
    private record WithFeatures<T extends MetaMachine>(MachineInstanceFactory<T> factory)
            implements MachineInstanceFactory<T> {

        @Override
        public T buildMachine(BlockEntityCreationInfo info) {
            T machine = factory.buildMachine(info);
            for (Created<?> created : CREATED) created.apply(machine);
            return machine;
        }
    }

    private static final List<Created<?>> CREATED = new CopyOnWriteArrayList<>();
    private static final List<Panel<?>> PANELS = new CopyOnWriteArrayList<>();

    private MachineFeatures() {}

    /** For every machine of {@code type} (subclasses too) once it is built; registered from a mod constructor. */
    public static <T extends MetaMachine> void onCreated(Class<T> type, Consumer<T> feature) {
        CREATED.add(new Created<>(type, feature));
    }

    /**
     * For every machine that runs {@code recipeType}, of whatever kind: the single block made for it, a modular
     * multiblock, another addon's machine. What a recipe type needs from the machine (an input that changes with every
     * craft, say) belongs here, so that no machine running the type goes without it.
     */
    public static void onRecipeType(Supplier<? extends GTRecipeType> recipeType, Consumer<MetaMachine> feature) {
        onCreated(MetaMachine.class, machine -> {
            if (machine instanceof IRecipeLogicMachine &&
                    Arrays.asList(machine.getDefinition().getRecipeTypes()).contains(recipeType.get())) {
                feature.accept(machine);
            }
        });
    }

    /** For the UI panel of every machine of {@code type} (subclasses too) once GTCEu has built it. */
    public static <T extends MetaMachine> void onPanel(Class<T> type, PanelFeature<T> feature) {
        PANELS.add(new Panel<>(type, feature));
    }

    /** For {@code MachineBuilderMixin}. */
    public static <T extends MetaMachine> MachineInstanceFactory<T> wrap(MachineInstanceFactory<T> factory) {
        return factory instanceof WithFeatures<T> ? factory : new WithFeatures<>(factory);
    }

    /** For {@code MachineUIPanelBuilderMixin}. */
    public static void panelBuilt(MetaMachine machine, MachineUIPanel panel, PanelSyncManager syncManager) {
        for (Panel<?> feature : PANELS) feature.apply(machine, panel, syncManager);
    }
}
