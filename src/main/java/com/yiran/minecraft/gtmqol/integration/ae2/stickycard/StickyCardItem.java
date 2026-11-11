package com.yiran.minecraft.gtmqol.integration.ae2.stickycard;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.items.materials.UpgradeCardItem;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.yiran.minecraft.gtmqol.GTMQoL;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;
import com.yiran.minecraft.gtmqol.integration.ae2.AEProcessing;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * Upgrade card for storage buses, ported from the v7 {@code StickyCardItem}. A storage bus with it keeps
 * partitioned content from falling through to equal or lower priority storage (see
 * {@code NetworkStorageMixin}). Only touch this class when AE2 is loaded ({@code GTCEu.Mods.isAE2Loaded()}).
 */
public class StickyCardItem extends UpgradeCardItem {

    private static final String TOOLTIP_KEY = "gtmqol.item.sticky_card.tooltip.";
    private static final String[] TOOLTIPS = {
            "Storage Bus upgrade. What matches the bus's partition goes only into that storage: when it is full, " +
                    "nothing overflows into storage of equal or lower priority.",
            "Needs a partition on the Storage Bus (whitelist or blacklist). Storage of higher priority still " +
                    "takes its share first."
    };

    /** Null unless {@code ae2.stickyCard} is on in the early config; the mixins check it. */
    public static ItemEntry<StickyCardItem> STICKY_CARD;

    public StickyCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        for (int i = 0; i < TOOLTIPS.length; i++) {
            lines.add(Component.translatable(TOOLTIP_KEY + i));
        }
    }

    public static void init() {
        STICKY_CARD = GTMQoLAddon.registrate().item("sticky_card", StickyCardItem::new)
                .lang("Sticky Card").register();
        for (int i = 0; i < TOOLTIPS.length; i++) {
            GTMQoLAddon.registrate().addRawLang(TOOLTIP_KEY + i, TOOLTIPS[i]);
        }
    }

    /** AE2 adds its own cards in its common setup; call this from ours (inside {@code enqueueWork}). */
    public static void registerUpgrades() {
        Upgrades.add(STICKY_CARD, AEParts.STORAGE_BUS, 1);
        if (GTCEu.isModLoaded("expatternprovider")) {
            Upgrades.add(STICKY_CARD, EPPItemAndBlock.MOD_STORAGE_BUS, 1);
            Upgrades.add(STICKY_CARD, EPPItemAndBlock.PRECISE_STORAGE_BUS, 1);
            Upgrades.add(STICKY_CARD, EPPItemAndBlock.TAG_STORAGE_BUS, 1);
        }
    }

    public static void addRecipes(Consumer<FinishedRecipe> provider, boolean meAssembler) {
        if (meAssembler) {
            AEProcessing.ME_ASSEMBLER_RECIPES.recipeBuilder(GTMQoL.id("sticky_card"))
                    .inputItems(AEItems.LOGIC_PROCESSOR.asItem(), 2)
                    .inputItems(Items.SLIME_BALL)
                    .inputItems(AEItems.ADVANCED_CARD.asItem())
                    .outputItems(STICKY_CARD.get())
                    .duration(20)
                    .EUt(GTValues.VA[GTValues.LV])
                    .save(provider);
        }
        VanillaRecipeHelper.addShapedRecipe(provider, GTMQoL.id("sticky_card_craft"),
                new ItemStack(STICKY_CARD.get()), " d ", " S ", "LCL",
                'S', Items.SLIME_BALL,
                'L', AEItems.LOGIC_PROCESSOR.asItem(),
                'C', AEItems.ADVANCED_CARD.asItem());
    }
}
