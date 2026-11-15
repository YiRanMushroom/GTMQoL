package com.yiran.minecraft.gtmqol.common.beacon;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A core for a Personal Beacon Core Socket: one level of its effect. A Potion Beacon Core keeps its effect's ID in
 * its NBT ({@link #EFFECT_KEY}; 1.21 uses a data component); the others have theirs fixed.
 */
public class BeaconCoreItem extends Item {

    public static final String EFFECT_KEY = "gtmqol:beacon_core_effect";

    private final @Nullable Supplier<MobEffect> effect;

    public BeaconCoreItem(Properties properties, @Nullable Supplier<MobEffect> effect) {
        super(properties.stacksTo(64));
        this.effect = effect;
    }

    /** The effect a core gives, or null for anything else (a Potion Beacon Core without its effect too). */
    public static @Nullable MobEffect effect(ItemStack stack) {
        if (!(stack.getItem() instanceof BeaconCoreItem core)) return null;
        return core.effect != null ? core.effect.get() : storedEffect(stack);
    }

    private static @Nullable MobEffect storedEffect(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(EFFECT_KEY, Tag.TAG_STRING)) return null;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(EFFECT_KEY));
        return id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
    }

    public static boolean isCore(ItemStack stack) {
        return effect(stack) != null;
    }

    /** A Potion Beacon Core of {@code effect}. */
    public static ItemStack potionCore(MobEffect effect) {
        ItemStack stack = PersonalBeacon.POTION_CORE.asStack();
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id != null) stack.getOrCreateTag().putString(EFFECT_KEY, id.toString());
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        MobEffect stored = effect == null ? storedEffect(stack) : null;
        if (stored == null) return super.getName(stack);
        return Component.translatable(PersonalBeacon.KEY + "potion_core.name", stored.getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        MobEffect given = effect(stack);
        if (given == null) return;
        lines.add(Component.translatable(PersonalBeacon.KEY + "core.tooltip", given.getDisplayName())
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(given == BeaconEffects.FLIGHT.get() ?
                PersonalBeacon.KEY + "core.tooltip.single" : PersonalBeacon.KEY + "core.tooltip.levels")
                .withStyle(ChatFormatting.GRAY));
    }
}
