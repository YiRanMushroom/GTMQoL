package com.yiran.minecraft.gtmqol.common.crafter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

import org.jetbrains.annotations.Nullable;

/**
 * Shows what a Crafting Pattern makes while Shift is held, as AE2 does with its encoded patterns: the pattern takes the
 * crafted item's model. Items drawn by a custom renderer (chests, shields) keep the pattern's look, since that renderer
 * would be handed the pattern and draw nothing.
 */
// Untested: holding Shift shows Crafting Patterns as what they craft (inventory, pattern crafter, patterns window)
@OnlyIn(Dist.CLIENT)
public final class CraftingPatternModel extends BakedModelWrapper<BakedModel> {

    private final ItemOverrides overrides;

    private CraftingPatternModel(BakedModel pattern) {
        super(pattern);
        ItemOverrides own = pattern.getOverrides();
        overrides = new ItemOverrides() {

            @Override
            public @Nullable BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                                @Nullable LivingEntity entity, int seed) {
                BakedModel product = Screen.hasShiftDown() ? productModel(stack, level, entity, seed) : null;
                return product != null ? product : own.resolve(model, stack, level, entity, seed);
            }
        };
    }

    public static void init(IEventBus modBus) {
        modBus.addListener((ModelEvent.ModifyBakingResult event) -> {
            if (PatternCrafter.PATTERN == null) return;
            event.getModels().computeIfPresent(ModelResourceLocation.inventory(PatternCrafter.PATTERN.getId()),
                    (location, model) -> new CraftingPatternModel(model));
        });
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private static @Nullable BakedModel productModel(ItemStack pattern, @Nullable ClientLevel level,
                                                     @Nullable LivingEntity entity, int seed) {
        ClientLevel world = level != null ? level : Minecraft.getInstance().level;
        if (world == null) return null;
        ICrafterPattern decoded = ICrafterPattern.decode(pattern, world);
        if (decoded == null || decoded.outputs().isEmpty()) return null;
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(decoded.outputs().get(0), world, entity,
                seed);
        return model.isCustomRenderer() ? null : model;
    }
}
