package com.yiran.minecraft.gtmqol.mixin_helper;

import com.gregtechceu.gtceu.api.recipe.lookup.RecipeDB;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RecipeDBStatic {
    public static final ThreadLocal<boolean[]> DISTINCT_FLAGS = new ThreadLocal<>();
    public static final ThreadLocal<int[]> BUS_IDS = new ThreadLocal<>();

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