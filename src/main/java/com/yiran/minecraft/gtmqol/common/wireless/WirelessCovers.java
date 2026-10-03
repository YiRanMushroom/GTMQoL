package com.yiran.minecraft.gtmqol.common.wireless;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.gregtechceu.gtceu.common.data.GTCovers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyCover;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyHatchPartMachine;
import com.yiran.minecraft.gtmqol.common.wireless.energy.WirelessEnergyMachines;
import com.yiran.minecraft.gtmqol.common.wireless.steam.WirelessSteamCover;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.List;
import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

/**
 * Wireless energy and steam covers and their items. Registered from GTCEu's cover {@code RegisterEvent}
 * ({@code GTCovers.init()}), which runs before items are registered, so the items can be made here too.
 */
public final class WirelessCovers {

    private static final String ENERGY_INPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_input_cover";
    private static final String ENERGY_OUTPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_output_cover";
    private static final String ENERGY_RATE_TOOLTIP_KEY = "gtmqol.wireless.tooltip.energy_cover_rate";
    private static final String STEAM_INPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_input_cover";
    private static final String STEAM_OUTPUT_TOOLTIP_KEY = "gtmqol.wireless.tooltip.steam_output_cover";
    private static final String COVER_BINDING_TOOLTIP_KEY = "gtmqol.wireless.tooltip.cover_binding";

    private static final ResourceLocation WIRELESS_OVERLAY = GTMQoL.id("block/overlay/machine/overlay_wireless");

    public static CoverDefinition[] ENERGY_INPUT = new CoverDefinition[TIER_COUNT];
    public static CoverDefinition[] ENERGY_OUTPUT = new CoverDefinition[TIER_COUNT];
    public static CoverDefinition STEAM_INPUT;
    public static CoverDefinition STEAM_OUTPUT;

    @SuppressWarnings("unchecked")
    public static ItemEntry<ComponentItem>[] ENERGY_INPUT_ITEM = new ItemEntry[TIER_COUNT];
    @SuppressWarnings("unchecked")
    public static ItemEntry<ComponentItem>[] ENERGY_OUTPUT_ITEM = new ItemEntry[TIER_COUNT];
    public static ItemEntry<ComponentItem> STEAM_INPUT_ITEM;
    public static ItemEntry<ComponentItem> STEAM_OUTPUT_ITEM;

    private WirelessCovers() {}

    public static void initEnergy() {
        for (int tier : GTMachineUtils.ALL_TIERS) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            ENERGY_INPUT[tier] = register("wireless_energy_input." + tierName,
                    (def, holder, side) -> new WirelessEnergyCover(def, holder, side, tier, IO.IN));
            ENERGY_OUTPUT[tier] = register("wireless_energy_output." + tierName,
                    (def, holder, side) -> new WirelessEnergyCover(def, holder, side, tier, IO.OUT));
            ENERGY_INPUT_ITEM[tier] = energyItem(tierName + "_wireless_energy_input_cover",
                    VNF[tier] + " Wireless Energy Input Cover", ENERGY_INPUT[tier], ENERGY_INPUT_TOOLTIP_KEY, tier);
            ENERGY_OUTPUT_ITEM[tier] = energyItem(tierName + "_wireless_energy_output_cover",
                    VNF[tier] + " Wireless Energy Output Cover", ENERGY_OUTPUT[tier], ENERGY_OUTPUT_TOOLTIP_KEY,
                    tier);
        }

        addLang(ENERGY_INPUT_TOOLTIP_KEY, "Charges the machine from the wireless network, at any input voltage");
        addLang(ENERGY_OUTPUT_TOOLTIP_KEY, "Sends the energy stored in a generator, battery buffer or other EU source to the wireless network");
        addLang(ENERGY_RATE_TOOLTIP_KEY, "Up to %s EU/t per ampere (%s)");
        addBindingLang();
    }

    public static void initSteam() {
        STEAM_INPUT = register("wireless_steam_input",
                (def, holder, side) -> new WirelessSteamCover(def, holder, side, IO.IN));
        STEAM_OUTPUT = register("wireless_steam_output",
                (def, holder, side) -> new WirelessSteamCover(def, holder, side, IO.OUT));
        STEAM_INPUT_ITEM = item("wireless_steam_input_cover", "Wireless Steam Input Cover", STEAM_INPUT,
                Component.translatable(STEAM_INPUT_TOOLTIP_KEY));
        STEAM_OUTPUT_ITEM = item("wireless_steam_output_cover", "Wireless Steam Output Cover", STEAM_OUTPUT,
                Component.translatable(STEAM_OUTPUT_TOOLTIP_KEY));

        addLang(STEAM_INPUT_TOOLTIP_KEY, "Fills the machine with steam from the wireless network");
        addLang(STEAM_OUTPUT_TOOLTIP_KEY, "Sends steam from the machine (e.g. a boiler) to the wireless network");
        addBindingLang();
    }

    private static boolean bindingLangAdded;

    // Shared by both kinds; the lang provider throws on a duplicate key.
    private static void addBindingLang() {
        if (bindingLangAdded) return;
        bindingLangAdded = true;
        addLang(COVER_BINDING_TOOLTIP_KEY, "Bound to whoever attaches it; screwdriver or shift-right-click with an empty hand opens the settings");
    }

    private static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behavior) {
        return GTCovers.register(GTMQoL.id(id), behavior, () -> () -> new SimpleCoverRenderer(WIRELESS_OVERLAY));
    }

    private static ItemEntry<ComponentItem> energyItem(String name, String lang, CoverDefinition cover,
                                                       String tooltipKey, int tier) {
        return item(name, lang, cover, Component.translatable(tooltipKey),
                Component.translatable(ENERGY_RATE_TOOLTIP_KEY, FormattingUtil.formatNumbers(V[tier]), VNF[tier]),
                Component.translatable(WirelessEnergyMachines.AMPERAGE_TOOLTIP_KEY,
                        WirelessEnergyHatchPartMachine.DEFAULT_AMPERAGE));
    }

    private static ItemEntry<ComponentItem> item(String name, String lang, CoverDefinition cover,
                                                 Component... tooltips) {
        return GTMQoLAddon.registrate()
                .item(name, ComponentItem::create)
                .lang(lang)
                .model((ctx, prov) -> prov.generated(ctx, WIRELESS_OVERLAY))
                .onRegister(attach(new TooltipBehavior(lines -> {
                    lines.addAll(List.of(tooltips));
                    lines.add(Component.translatable(COVER_BINDING_TOOLTIP_KEY));
                }), new CoverPlaceBehavior(cover)))
                .register();
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
