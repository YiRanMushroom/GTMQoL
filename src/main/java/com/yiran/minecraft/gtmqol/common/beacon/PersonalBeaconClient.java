package com.yiran.minecraft.gtmqol.common.beacon;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Client side of the Personal Beacon: cores tinted in their effect's colour. */
public final class PersonalBeaconClient {

    private PersonalBeaconClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterColorHandlersEvent.Item event) -> {
            event.register((stack, layer) -> layer == 0 ? color(stack) : -1, PersonalBeacon.POTION_CORE.get());
            for (var core : PersonalBeacon.EFFECT_CORES) {
                event.register((stack, layer) -> layer == 0 ? color(stack) : -1, core.get());
            }
        });
    }

    private static int color(ItemStack stack) {
        MobEffect effect = BeaconCoreItem.effect(stack);
        return effect == null ? -1 : 0xFF000000 | effect.getColor();
    }
}
