package com.yiran.minecraft.gtmqol.common.beacon;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Client side of the Personal Beacon: cores tinted in their effect's colour. */
public final class PersonalBeaconClient {

    private PersonalBeaconClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterColorHandlersEvent.Item.class, event -> {
            event.register((stack, layer) -> layer == 0 ? color(stack) : -1, PersonalBeacon.POTION_CORE.get());
            for (var core : PersonalBeacon.EFFECT_CORES) {
                event.register((stack, layer) -> layer == 0 ? color(stack) : -1, core.get());
            }
        });
    }

    private static int color(ItemStack stack) {
        Holder<MobEffect> effect = BeaconCoreItem.effect(stack);
        return effect == null ? -1 : 0xFF000000 | effect.value().getColor();
    }
}
