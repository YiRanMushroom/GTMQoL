package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEGuiHelper;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import appeng.api.client.AEKeyRendering;
import appeng.api.integrations.emi.EmiStackConverters;
import appeng.api.integrations.jei.IngredientConverter;
import appeng.api.integrations.jei.IngredientConverters;
import appeng.api.stacks.GenericStack;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Config of {@link MEUniversalInputPartMachine}: like gtceu's {@code AEConfigWidget} (a config cell over a stock
 * cell), but any AE key, drawn by AE's own renderers. 9 columns; put it in a scrolling parent.
 * <p>
 * Left click sets the held item, right click sets what the held container holds (fluid, chemical, ...) or clears
 * the slot. Recipe viewer drags go through AE's JEI / EMI converters, so every key type an addon registers works.
 * The ghost slot takes any ingredient ({@code Object}): JEI asks {@link #castGhostIngredientIfValid} for each one,
 * EMI goes through {@link #ingredientHandlingOverride}.
 */
public class UniversalConfigWidget extends Widget<UniversalConfigWidget>
                                   implements Interactable, GhostIngredientSlot<Object> {

    public static final String PREFIX = "gtmqol_universal_";
    /** Index, mouse button. */
    public static final String CLICK = PREFIX + "config_click";
    /** Index, a {@link GenericStack}. */
    public static final String SET_GHOST = PREFIX + "config_set_ghost";
    private static final String DISPLAY = PREFIX + "config_display";

    public static final int COLUMNS = 9;
    private static final int CELL_SIZE = 18;
    public static final int PAIR_HEIGHT = CELL_SIZE * 2 + 2;

    private static final boolean EMI_LOADED = ModList.get().isLoaded("emi");
    private static final boolean JEI_LOADED = ModList.get().isLoaded("jei");

    private final IConfigurableSlotList slotList;
    private final int slotCount;
    private final BooleanSupplier autoPull;
    private @Nullable PanelSyncManager syncManager;
    private @Nullable AEConfigSyncHandler configSyncHandler;

    // Local and already scrolled: the parent's scroll is a transform of the context.
    @OnlyIn(Dist.CLIENT)
    private float lastMouseX;
    @OnlyIn(Dist.CLIENT)
    private float lastMouseY;

    public UniversalConfigWidget(IConfigurableSlotList slotList, int slotCount, BooleanSupplier autoPull) {
        this.slotList = slotList;
        this.slotCount = slotCount;
        this.autoPull = autoPull;
        size(COLUMNS * CELL_SIZE, rows(slotCount) * PAIR_HEIGHT);
    }

    public static int rows(int slotCount) {
        return (slotCount + COLUMNS - 1) / COLUMNS;
    }

    public UniversalConfigWidget syncManager(PanelSyncManager syncManager) {
        this.syncManager = syncManager;
        this.configSyncHandler = new AEConfigSyncHandler(slotList, slotCount);
        syncManager.syncValue(DISPLAY, configSyncHandler);
        return this;
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    private int slotX(int index) {
        return (index % COLUMNS) * CELL_SIZE;
    }

    private int slotY(int index) {
        return (index / COLUMNS) * PAIR_HEIGHT;
    }

    private @Nullable GenericStack clientConfig(int index) {
        return configSyncHandler == null ? null : configSyncHandler.getClientConfig(index);
    }

    private @Nullable GenericStack clientStock(int index) {
        return configSyncHandler == null ? null : configSyncHandler.getClientStock(index);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
        Minecraft mc = Minecraft.getInstance();
        boolean pulling = autoPull.getAsBoolean();
        lastMouseX = context.getMouseX();
        lastMouseY = context.getMouseY();

        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);
            if (pulling) {
                GTGuiTextures.SLOT_DARK.draw(context, x, y, 18, 18);
                GTGuiTextures.CONFIG_ARROW.draw(context, x, y, 18, 18);
            } else {
                GTGuiTextures.SLOT.draw(context, x, y, 18, 18);
                GTGuiTextures.CONFIG_ARROW_DARK.draw(context, x, y, 18, 18);
            }
            GTGuiTextures.SLOT_DARK.draw(context, x, y + 18, 18, 18);

            GenericStack config = clientConfig(i);
            GenericStack stock = clientStock(i);
            if (config != null) AEKeyRendering.drawInGui(mc, graphics, x + 1, y + 1, config.what());
            if (stock != null) {
                AEKeyRendering.drawInGui(mc, graphics, x + 1, y + 19, stock.what());
                AEGuiHelper.drawAmountOverlay(graphics, stock.amount(), x + 1, y + 19);
            }

            if (lastMouseX >= x && lastMouseX < x + CELL_SIZE) {
                if (lastMouseY >= y && lastMouseY < y + CELL_SIZE) {
                    AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 1, 16, 16);
                } else if (lastMouseY >= y + CELL_SIZE && lastMouseY < y + CELL_SIZE * 2) {
                    AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 19, 16, 16);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering()) return;
        float mouseX = context.getMouseX();
        float mouseY = context.getMouseY();
        int index = getSlotAtLocal(mouseX, mouseY);
        if (index < 0) return;
        boolean overConfig = mouseY < slotY(index) + CELL_SIZE;
        GenericStack stack = overConfig ? clientConfig(index) : clientStock(index);
        if (stack == null) return;
        List<Component> lines = new ArrayList<>(AEKeyRendering.getTooltip(stack.what()));
        if (!overConfig) {
            lines.add(Component.literal("x" + AEGuiHelper.formatAmountFull(stack.amount()))
                    .withStyle(ChatFormatting.GRAY));
        }
        context.getGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines,
                context.getAbsMouseX(), context.getAbsMouseY());
    }

    @Override
    public Result onMousePressed(int button) {
        if (autoPull.getAsBoolean() || syncManager == null) return Result.IGNORE;
        double localX = getContext().getMouseX();
        double localY = getContext().getMouseY();
        int index = getSlotAtLocal(localX, localY);
        if (index < 0 || localY >= slotY(index) + CELL_SIZE) return Result.IGNORE;
        if (button != 0 && button != 1) return Result.IGNORE;
        syncManager.callSyncedAction(CLICK, buf -> {
            buf.writeVarInt(index);
            buf.writeVarInt(button);
        });
        return Result.SUCCESS;
    }

    // --- GhostIngredientSlot ---

    @Override
    public void setGhostIngredient(Object ingredient) {
        GenericStack stack = toGenericStack(ingredient);
        if (stack != null) sendGhost(stack);
    }

    @Override
    public boolean ingredientHandlingOverride(Object ingredient) {
        if (!EMI_LOADED || autoPull.getAsBoolean() || syncManager == null) return false;
        GenericStack stack = fromEmi(ingredient);
        if (stack == null) return false;
        sendGhost(stack);
        return true;
    }

    private static @Nullable GenericStack toGenericStack(Object ingredient) {
        if (ingredient instanceof ItemStack itemStack) return GenericStack.fromItemStack(itemStack);
        if (JEI_LOADED) {
            GenericStack stack = fromJei(ingredient);
            if (stack != null) return stack;
        }
        return EMI_LOADED ? fromEmi(ingredient) : null;
    }

    // AE's own JEI ghost handler does the same (GenericEntryStackHelper, not API).
    private static @Nullable GenericStack fromJei(Object ingredient) {
        for (var converter : IngredientConverters.getConverters()) {
            GenericStack stack = fromJei(converter, ingredient);
            if (stack != null) return stack;
        }
        return null;
    }

    private static <T> @Nullable GenericStack fromJei(IngredientConverter<T> converter, Object ingredient) {
        Class<? extends T> type = converter.getIngredientType().getIngredientClass();
        return type.isInstance(ingredient) ? converter.getStackFromIngredient(type.cast(ingredient)) : null;
    }

    private static @Nullable GenericStack fromEmi(Object ingredient) {
        if (!(ingredient instanceof EmiStack emiStack)) return null;
        for (var converter : EmiStackConverters.getConverters()) {
            GenericStack stack = converter.toGenericStack(emiStack);
            if (stack != null) return stack;
        }
        return null;
    }

    private void sendGhost(GenericStack stack) {
        if (autoPull.getAsBoolean() || syncManager == null) return;
        int index = findTargetSlot();
        syncManager.callSyncedAction(SET_GHOST, buf -> {
            buf.writeVarInt(index);
            GenericStack.writeBuffer(stack, buf);
        });
    }

    @Override
    public @Nullable Object castGhostIngredientIfValid(Object ingredient) {
        if (autoPull.getAsBoolean() || !areAncestorsEnabled()) return null;
        return toGenericStack(ingredient) != null ? ingredient : null;
    }

    @Override
    public Class<Object> ingredientClass() {
        return Object.class;
    }

    // --- Internals ---

    private int findTargetSlot() {
        int hovered = getSlotAtLocal(lastMouseX, lastMouseY);
        if (hovered >= 0) return hovered;
        for (int i = 0; i < slotCount; i++) {
            if (clientConfig(i) == null) return i;
        }
        return 0;
    }

    private int getSlotAtLocal(double localX, double localY) {
        if (localX < 0 || localY < 0) return -1;
        int column = (int) (localX / CELL_SIZE);
        int row = (int) (localY / PAIR_HEIGHT);
        if (column >= COLUMNS || localY - row * PAIR_HEIGHT >= CELL_SIZE * 2) return -1;
        int index = row * COLUMNS + column;
        return index < slotCount ? index : -1;
    }
}
