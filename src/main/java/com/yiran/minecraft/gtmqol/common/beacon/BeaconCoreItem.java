package com.yiran.minecraft.gtmqol.common.beacon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A core for a Personal Beacon Core Socket: one level of its effect. A Potion Beacon Core keeps its effect in
 * {@link PersonalBeacon#CORE_EFFECT}; the others have theirs fixed.
 */
public class BeaconCoreItem extends Item {

    private final @Nullable Supplier<Holder<MobEffect>> effect;

    public BeaconCoreItem(Properties properties, @Nullable Supplier<Holder<MobEffect>> effect) {
        super(properties.stacksTo(64));
        this.effect = effect;
    }

    /** The effect a core gives, or null for anything else (a Potion Beacon Core without its effect too). */
    public static @Nullable Holder<MobEffect> effect(ItemStack stack) {
        if (!(stack.getItem() instanceof BeaconCoreItem core)) return null;
        return core.effect != null ? core.effect.get() : stack.get(PersonalBeacon.CORE_EFFECT.get());
    }

    public static boolean isCore(ItemStack stack) {
        return effect(stack) != null;
    }

    /** A Potion Beacon Core of {@code effect}. */
    public static ItemStack potionCore(Holder<MobEffect> effect) {
        ItemStack stack = PersonalBeacon.POTION_CORE.asStack();
        stack.set(PersonalBeacon.CORE_EFFECT.get(), effect);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        Holder<MobEffect> stored = effect == null ? stack.get(PersonalBeacon.CORE_EFFECT.get()) : null;
        if (stored == null) return super.getName(stack);
        return Component.translatable(PersonalBeacon.KEY + "potion_core.name", stored.value().getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        Holder<MobEffect> holder = effect(stack);
        if (holder == null) return;
        lines.add(Component.translatable(PersonalBeacon.KEY + "core.tooltip", holder.value().getDisplayName())
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(holder.value() == BeaconEffects.FLIGHT.get() ?
                PersonalBeacon.KEY + "core.tooltip.single" : PersonalBeacon.KEY + "core.tooltip.levels")
                .withStyle(ChatFormatting.GRAY));
    }
}
