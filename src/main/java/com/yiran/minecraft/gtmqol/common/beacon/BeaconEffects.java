package com.yiran.minecraft.gtmqol.common.beacon;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.Nullable;

/**
 * The Personal Beacon's effects that vanilla has no potion or effect for: Flight (creative flight while it lasts)
 * and Flight Speed (faster flying, per level).
 */
public final class BeaconEffects {

    /** Vanilla's flying speed. */
    public static final float BASE_FLYING_SPEED = 0.05f;
    /** Flight Speed adds this share of {@link #BASE_FLYING_SPEED} per level. */
    public static final float FLYING_SPEED_PER_LEVEL = 0.5f;

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,
            GTMQoL.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT = EFFECTS.register("flight", FlightEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT_SPEED = EFFECTS.register("flight_speed",
            FlightSpeedEffect::new);

    private BeaconEffects() {}

    public static void init(IEventBus modBus) {
        EFFECTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(MobEffectEvent.Remove.class,
                event -> onEnded(event.getEntity(), event.getEffectInstance() == null ? null :
                        event.getEffectInstance().getEffect().value()));
        NeoForge.EVENT_BUS.addListener(MobEffectEvent.Expired.class,
                event -> onEnded(event.getEntity(), event.getEffectInstance() == null ? null :
                        event.getEffectInstance().getEffect().value()));
    }

    /** Flight Speed ended: back to vanilla's speed. */
    private static void onEnded(LivingEntity entity, @Nullable MobEffect effect) {
        if (effect != FLIGHT_SPEED.get() || !(entity instanceof Player player) || player.level().isClientSide) return;
        player.getAbilities().setFlyingSpeed(BASE_FLYING_SPEED);
        player.onUpdateAbilities();
    }

    /** NeoForge's creative flight attribute: the player may fly while it is above 0. */
    private static final class FlightEffect extends MobEffect {

        FlightEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xD8F0FF);
            addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT, GTMQoL.id("effect.flight"), 1,
                    AttributeModifier.Operation.ADD_VALUE);
        }
    }

    /** Players have no flying speed attribute: their abilities' speed is set while it lasts. */
    private static final class FlightSpeedEffect extends MobEffect {

        FlightSpeedEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x7FC8FF);
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player && !player.level().isClientSide) {
                float speed = BASE_FLYING_SPEED * (1 + FLYING_SPEED_PER_LEVEL * (amplifier + 1));
                if (player.getAbilities().getFlyingSpeed() != speed) {
                    player.getAbilities().setFlyingSpeed(speed);
                    player.onUpdateAbilities();
                }
            }
            return true;
        }
    }
}
