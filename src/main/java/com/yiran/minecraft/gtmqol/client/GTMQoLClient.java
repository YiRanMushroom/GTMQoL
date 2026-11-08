package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalEmiConverter;
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;

import net.minecraftforge.fml.ModList;

/** Called from the mod constructor on the client only, like gtceu's {@code ClientProxy}. */
public final class GTMQoLClient {

    private GTMQoLClient() {}

    public static void init() {
        // Must be registered before machine models are generated or loaded, they reference the type by id.
        DynamicRenderManager.register(GTMQoL.id("dtfr_ring"), DTFRRingRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("fishing_pond_water"), FishingPondWaterRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("casing_part"), CasingPartRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("energy_cube_sides"), EnergyCubeSidesRender.TYPE);
        if (GTCEu.Mods.isAE2Loaded()) {
            DynamicRenderManager.register(GTMQoL.id("energy_cell_sides"), EnergyCellSidesRender.TYPE);
            DynamicRenderManager.register(GTMQoL.id("matrix_part"), AssemblingMatrixPartRender.TYPE);
        }
        // Mekanism 10.4's chemicals only exist in EMI through JEMI, see ChemicalEmiConverter.
        if (ModList.get().isLoaded("emi") && ModList.get().isLoaded("jei") && ModList.get().isLoaded("mekanism")) {
            ChemicalEmiConverter.register();
        }
        GregificationClient.init();
    }
}
