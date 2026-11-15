package com.yiran.minecraft.gtmqol.integration.ae2.cablecover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AEColor;
import appeng.capabilities.Capabilities;
import appeng.core.definitions.AEParts;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.L;
import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

/**
 * The ME Cable Cover: its registration, its recipe, and how AE2 finds the node of a {@link MECableCover} in a machine
 * that has no ME connection of its own. Only touch this class when AE2 is loaded.
 */
public final class MECableCovers {

    private static final String TOOLTIP_KEY = "gtmqol.me_cable_cover.tooltip.";
    /** GTCEu's, as only its and AE2's textures may be used: the port of an ME bus. */
    private static final ResourceLocation OVERLAY = GTCEu.id("block/overlay/appeng/me_output_bus");

    public static CoverDefinition COVER;
    public static ItemEntry<ComponentItem> ITEM;

    private MECableCovers() {}

    /** From GTCEu's cover registration event, which runs before items are registered. */
    public static void init() {
        COVER = GTCovers.register(GTMQoL.id("me_cable"), MECableCover::new,
                () -> () -> new SimpleCoverRenderer(OVERLAY));
        registerItem();
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, MECableCovers::onAttachCapabilities);
    }

    private static void registerItem() {
        ITEM = GTMQoLAddon.registrate()
                .item("me_cable_cover", ComponentItem::create)
                .lang("ME Cable Cover")
                .model((ctx, prov) -> prov.generated(ctx, OVERLAY))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.add(Component.translatable(TOOLTIP_KEY + "0"));
                    lines.add(Component.translatable(TOOLTIP_KEY + "1"));
                }), new CoverPlaceBehavior(COVER)))
                .register();
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + "0",
                "The machine carries ME channels on all sides, like an ME Dense Cable");
        GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + "1",
                "On a machine with an ME connection: that connects on all sides");
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider) {
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("me_cable_cover"))
                .inputItems(AEParts.CABLE_ANCHOR.asItem())
                .inputItems(AEParts.COVERED_DENSE_CABLE.item(AEColor.TRANSPARENT))
                .inputItems(TagPrefix.plate, GTMaterials.Steel)
                .inputItems(CustomTags.LV_CIRCUITS)
                .inputFluids(GTMaterials.SolderingAlloy, L / 2)
                .outputItems(ITEM)
                .duration(100)
                .EUt(VA[LV])
                .save(provider);
    }

    /** The cover of this machine that has a node of its own, which is what AE2 connects to there. */
    private static @Nullable IInWorldGridNodeHost host(MetaMachine machine) {
        for (CoverBehavior cover : machine.getCoverContainer().getCovers()) {
            if (cover instanceof MECableCover cable && cable.hasNode()) return cable;
        }
        return null;
    }

    /**
     * A machine asks its attached capabilities after its own, and its own only answer for machines with an ME
     * connection: those never have a cover with a node.
     */
    private static void onAttachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof MetaMachine machine)) return;
        event.addCapability(GTMQoL.id("me_cable_cover"), new ICapabilityProvider() {

            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap != Capabilities.IN_WORLD_GRID_NODE_HOST) return LazyOptional.empty();
                IInWorldGridNodeHost host = host(machine);
                return host == null ? LazyOptional.empty() : LazyOptional.of(() -> host).cast();
            }
        });
    }
}
