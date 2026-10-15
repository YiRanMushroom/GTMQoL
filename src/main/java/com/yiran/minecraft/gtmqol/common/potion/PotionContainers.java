package com.yiran.minecraft.gtmqol.common.potion;

import com.yiran.minecraft.gtmqol.GTMQoLAddon;

/** Potion fluids. Splash / lingering potions in the brewery are 1.21 only so far. */
public final class PotionContainers {

    /** Recipe viewer hint on a potion input slot, which shows only gtceu's potion fluid. */
    public static final String ANY_MOD_KEY = "gtmqol.gui.content.any_mod_fluid";

    private PotionContainers() {}

    public static void init() {
        GTMQoLAddon.registrate().addRawLang(ANY_MOD_KEY, "Any mod's version works");
    }
}
