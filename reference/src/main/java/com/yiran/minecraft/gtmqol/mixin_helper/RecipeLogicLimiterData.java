package com.yiran.minecraft.gtmqol.mixin_helper;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import java.util.HashSet;
import java.util.Set;

public class RecipeLogicLimiterData {
    private static final Set<IRecipeLogicMachine> visited = new HashSet<IRecipeLogicMachine>();

    public static boolean shouldTick(IRecipeLogicMachine machine) {
        if (visited.contains(machine)) {
            return false;
        } else {
            visited.add(machine);
            return true;
        }
    }

    public static void onTickStarted() {
        visited.clear();
    }
}
