package com.yiran.minecraft.gtmqol.common.beacon;

import com.yiran.minecraft.gtmqol.GTMQoL;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.Nullable;

/**
 * The Personal Beacon's effects that vanilla has no potion or effect for: Flight (creative flight while it lasts)
 * and Flight Speed (faster flying, per level).
 */
public final class BeaconEffects {

    // Untested: Flight lets a survival player fly and ends with the effect (falling); Flight Speed makes flying faster
    // per level and flying speed is back to normal when it ends, also after death
    /** Vanilla's flying speed. */
    public static final float BASE_FLYING_SPEED = 0.05f;
    /** Flight Speed adds this share of {@link #BASE_FLYING_SPEED} per level. */
    public static final float FLYING_SPEED_PER_LEVEL = 0.5f;

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            GTMQoL.MOD_ID);

    public static final RegistryObject<MobEffect> FLIGHT = EFFECTS.register("flight", FlightEffect::new);
    public static final RegistryObject<MobEffect> FLIGHT_SPEED = EFFECTS.register("flight_speed",
            FlightSpeedEffect::new);

    private BeaconEffects() {}

    public static void init(IEventBus modBus) {
        EFFECTS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener((MobEffectEvent.Remove event) -> onEnded(event.getEntity(),
                event.getEffect()));
        MinecraftForge.EVENT_BUS.addListener((MobEffectEvent.Expired event) -> onEnded(event.getEntity(),
                event.getEffectInstance() == null ? null : event.getEffectInstance().getEffect()));
    }

    /** Flight or Flight Speed ended: back to how vanilla has it. */
    private static void onEnded(LivingEntity entity, @Nullable MobEffect effect) {
        if (!(entity instanceof Player player) || player.level().isClientSide) return;
        if (effect == FLIGHT.get()) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
        } else if (effect == FLIGHT_SPEED.get()) {
            player.getAbilities().setFlyingSpeed(BASE_FLYING_SPEED);
        } else {
            return;
        }
        player.onUpdateAbilities();
    }

    /** Forge 47 has no creative flight attribute: the player may fly while it lasts. */
    private static final class FlightEffect extends MobEffect {

        FlightEffect() {
            super(MobEffectCategory.BENEFICIAL, 0xD8F0FF);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player && !player.level().isClientSide && !player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    /** Players have no flying speed attribute: their abilities' speed is set while it lasts. */
    private static final class FlightSpeedEffect extends MobEffect {

        FlightSpeedEffect() {
            super(MobEffectCategory.BENEFICIAL, 0x7FC8FF);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (entity instanceof Player player && !player.level().isClientSide) {
                float speed = BASE_FLYING_SPEED * (1 + FLYING_SPEED_PER_LEVEL * (amplifier + 1));
                if (player.getAbilities().getFlyingSpeed() != speed) {
                    player.getAbilities().setFlyingSpeed(speed);
                    player.onUpdateAbilities();
                }
            }
        }
    }
}
