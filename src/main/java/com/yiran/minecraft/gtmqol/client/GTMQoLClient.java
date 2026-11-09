package com.yiran.minecraft.gtmqol.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.common.crafter.CraftingPatternModel;
import com.yiran.minecraft.gtmqol.common.crafter.DecodedPatterns;
import com.yiran.minecraft.gtmqol.common.multiblock.DTFRRingRender;
import com.yiran.minecraft.gtmqol.common.multiblock.IndustrialFishingPondWaterRender;
import com.yiran.minecraft.gtmqol.common.stacklike.mekanism.ChemicalEmiConverter;
import com.yiran.minecraft.gtmqol.common.wireless.energy.EnergyCubeSidesRender;
import com.yiran.minecraft.gtmqol.common.wireless.energy.ae2.EnergyCellSidesRender;
import com.yiran.minecraft.gtmqol.gregification.CasingPartRender;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixPartRender;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Called from the mod constructor on the client only, like gtceu's {@code ClientProxy}. */
public final class GTMQoLClient {

    private GTMQoLClient() {}

    public static void init(IEventBus modBus) {
        // Must be registered before machine models are generated or loaded, they reference the type by id.
        DynamicRenderManager.register(GTMQoL.id("dtfr_ring"), DTFRRingRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("industrial_fishing_pond_water"), IndustrialFishingPondWaterRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("casing_part"), CasingPartRender.TYPE);
        DynamicRenderManager.register(GTMQoL.id("energy_cube_sides"), EnergyCubeSidesRender.TYPE);
        if (GTCEu.Mods.isAE2Loaded()) {
            DynamicRenderManager.register(GTMQoL.id("energy_cell_sides"), EnergyCellSidesRender.TYPE);
            DynamicRenderManager.register(GTMQoL.id("matrix_part"), AssemblingMatrixPartRender.TYPE);
        }
        if (ModList.get().isLoaded("emi") && ModList.get().isLoaded("mekanism")) ChemicalEmiConverter.register();
        ShiftDragQuickMove.init();
        CraftingPatternModel.init(modBus);
        // the client keeps its recipe manager when the server sends new recipes
        NeoForge.EVENT_BUS.addListener(RecipesUpdatedEvent.class,
                event -> DecodedPatterns.clear(event.getRecipeManager()));
    }
}
