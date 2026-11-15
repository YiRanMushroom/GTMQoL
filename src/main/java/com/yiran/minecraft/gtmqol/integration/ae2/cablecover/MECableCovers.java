package com.yiran.minecraft.gtmqol.integration.ae2.cablecover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEParts;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.Nullable;

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

    public static Holder<CoverDefinition> COVER;
    public static ItemEntry<ComponentItem> ITEM;

    private MECableCovers() {}

    /** From the mod constructor. */
    public static void init(IEventBus modBus) {
        COVER = GTMQoLAddon.registrate().cover("me_cable", MECableCover::new,
                () -> () -> new SimpleCoverRenderer(OVERLAY));
        registerItem();
        modBus.addListener(MECableCovers::onRegisterCapabilities);
    }

    private static void registerItem() {
        ITEM = GTMQoLAddon.registrate()
                .item("me_cable_cover", ComponentItem::new)
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

    public static void addRecipes(RecipeOutput provider) {
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
     * Next to GTCEu's own provider for its machine blocks, which only answers for machines with an ME connection: those
     * never have a cover with a node, so it doesn't matter which of the two is asked first.
     */
    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        Block[] machines = BuiltInRegistries.BLOCK.stream()
                .filter(block -> block instanceof MetaMachineBlock)
                .toArray(Block[]::new);
        event.registerBlock(AECapabilities.IN_WORLD_GRID_NODE_HOST,
                (level, pos, state, blockEntity, side) -> blockEntity instanceof MetaMachine machine ? host(machine) :
                        null,
                machines);
    }
}
