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
import com.yiran.minecraft.gtmqol.gregification.client.GregificationClient;
import com.yiran.minecraft.gtmqol.integration.ae2.matrix.AssemblingMatrixPartRender;

import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Called from the mod constructor on the client only, like gtceu's {@code ClientProxy}. */
public final class GTMQoLClient {

    private GTMQoLClient() {}

    /** The client's main thread. */
    public static BlockableEventLoop<?> executor() {
        return Minecraft.getInstance();
    }

    /** For recipe logic in common code, which must not load client classes itself. */
    public static @Nullable Level level() {
        return Minecraft.getInstance().level;
    }

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
        // Mekanism 10.4's chemicals only exist in EMI through JEMI, see ChemicalEmiConverter.
        if (ModList.get().isLoaded("emi") && ModList.get().isLoaded("jei") && ModList.get().isLoaded("mekanism")) {
            ChemicalEmiConverter.register();
        }
        GregificationClient.init();
        ShiftDragQuickMove.init();
        CraftingPatternModel.init(modBus);
        // the client keeps its recipe manager when the server sends new recipes
        MinecraftForge.EVENT_BUS.addListener((RecipesUpdatedEvent event) -> DecodedPatterns.clear(event.getRecipeManager()));
    }
}
