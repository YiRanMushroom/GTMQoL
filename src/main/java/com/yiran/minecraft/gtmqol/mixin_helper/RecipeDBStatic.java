package com.yiran.minecraft.gtmqol.mixin_helper;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class RecipeDBStatic {
    // 记录所有开启了 Distinct 的机器实例
    public static final ThreadLocal<Set<MetaMachine>> DISTINCT_MACHINES = new ThreadLocal<>();
    // 记录每个槽位索引属于哪台机器
    public static final ThreadLocal<MetaMachine[]> INDEX_TO_MACHINE = new ThreadLocal<>();
    // 独立机器的私有查询表：MetaMachine -> 它拥有的所有槽位索引
    public static final ThreadLocal<Map<MetaMachine, int[]>> MACHINE_LOOKUP = new ThreadLocal<>();
    // 普通格位池，供非 Distinct 机器使用
    public static final ThreadLocal<int[]> NORMAL_POOL = new ThreadLocal<>();

    public static Method nodesForIngredientMethod;
    public static Constructor<?> searchFrameConstructor;
    public static Field fIndex;
    public static Field fIngredientIndex;
    public static Field fBranch;

    static {
        try {
            Class<?> branchClass = Class.forName("com.gregtechceu.gtceu.api.recipe.lookup.Branch");
            nodesForIngredientMethod = RecipeDB.class.getDeclaredMethod("nodesForIngredient", AbstractMapIngredient.class, branchClass);
            nodesForIngredientMethod.setAccessible(true);

            Class<?> searchFrameClass = Class.forName("com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB$SearchFrame");
            searchFrameConstructor = searchFrameClass.getDeclaredConstructor(int.class, branchClass);
            searchFrameConstructor.setAccessible(true);

            fIndex = searchFrameClass.getDeclaredField("index");
            fIndex.setAccessible(true);
            fIngredientIndex = searchFrameClass.getDeclaredField("ingredientIndex");
            fIngredientIndex.setAccessible(true);
            fBranch = searchFrameClass.getDeclaredField("branch");
            fBranch.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}