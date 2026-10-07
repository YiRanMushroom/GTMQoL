package com.yiran.minecraft.gtmqol.integration.ae2;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEGuiHelper;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.stacks.GenericStack;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.recipeviewer.handlers.GhostIngredientSlot;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * GTCEu's {@code AEConfigWidget} for a stocking fluid list, with its sync names prefixed with {@link #PREFIX}: the
 * original names are fixed, so it can't sit in the same panel as the item list's widget. Stocking lists ignore
 * amounts, so the amount editor and scrolling are left out. Layout is the same: 8 columns of a config cell over a
 * stock cell.
 */
public class FluidConfigWidget extends Widget<FluidConfigWidget>
                               implements Interactable, GhostIngredientSlot<ItemStack> {

    public static final String PREFIX = "gtmqol_fluid_";
    public static final String SET = PREFIX + "config_set";
    public static final String CLEAR = PREFIX + "config_clear";
    public static final String SET_GHOST = PREFIX + "config_set_ghost";
    private static final String DISPLAY = PREFIX + "config_display";

    private static final int CELL_SIZE = 18;
    private static final int PAIR_HEIGHT = CELL_SIZE * 2 + 2;
    private static final int COLUMNS = 8;

    private final IConfigurableSlotList slotList;
    private final int slotCount;
    private final BooleanSupplier autoPull;
    private @Nullable PanelSyncManager syncManager;
    private @Nullable AEConfigSyncHandler configSyncHandler;

    @OnlyIn(Dist.CLIENT)
    private float lastMouseX;
    @OnlyIn(Dist.CLIENT)
    private float lastMouseY;

    public FluidConfigWidget(IConfigurableSlotList slotList, int slotCount, BooleanSupplier autoPull) {
        this.slotList = slotList;
        this.slotCount = slotCount;
        this.autoPull = autoPull;
    }

    public FluidConfigWidget syncManager(PanelSyncManager syncManager) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
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
                GTGuiTextures.FLUID_SLOT.draw(context, x, y, 18, 18);
                GTGuiTextures.CONFIG_ARROW_DARK.draw(context, x, y, 18, 18);
            }
            GTGuiTextures.SLOT_DARK.draw(context, x, y + 18, 18, 18);

            GenericStack config = configSyncHandler != null ? configSyncHandler.getClientConfig(i) : null;
            GenericStack stock = configSyncHandler != null ? configSyncHandler.getClientStock(i) : null;
            if (config != null) AEGuiHelper.drawFluid(graphics, config, x + 1, y + 1);
            if (stock != null) {
                AEGuiHelper.drawFluid(graphics, stock, x + 1, y + 19);
                AEGuiHelper.drawAmountOverlay(graphics, stock.amount(), x + 1, y + 19);
            }

            float mouseX = context.getMouseX();
            float mouseY = context.getMouseY();
            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 1, 16, 16);
            } else if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y + CELL_SIZE &&
                    mouseY < y + CELL_SIZE * 2) {
                AEGuiHelper.drawSelectionOverlay(graphics, x + 1, y + 19, 16, 16);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawForeground(ModularGuiContext context) {
        float mouseX = context.getMouseX();
        float mouseY = context.getMouseY();
        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);
            if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE * 2) {
                GenericStack tooltipStack = configSyncHandler == null ? null : mouseY < y + CELL_SIZE ?
                        configSyncHandler.getClientConfig(i) : configSyncHandler.getClientStock(i);
                if (tooltipStack != null) {
                    ItemStack wrapped = GenericStack.wrapInItemStack(tooltipStack);
                    context.getGraphics().renderTooltip(Minecraft.getInstance().font, wrapped,
                            context.getAbsMouseX(), context.getAbsMouseY());
                }
            }
        }
    }

    @Override
    public Result onMousePressed(int button) {
        double localX = getContext().getMouseX();
        double localY = getContext().getMouseY();
        int slotIndex = getSlotAtLocal(localX, localY);
        if (autoPull.getAsBoolean() || syncManager == null) return Result.IGNORE;
        if (slotIndex < 0 || localY >= slotY(slotIndex) + CELL_SIZE) return Result.IGNORE;

        if (button == 1) {
            syncManager.callSyncedAction(CLEAR, buf -> buf.writeVarInt(slotIndex));
            return Result.SUCCESS;
        } else if (button == 0) {
            syncManager.callSyncedAction(SET, buf -> buf.writeVarInt(slotIndex));
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    // --- GhostIngredientSlot ---

    @Override
    public void setGhostIngredient(ItemStack ingredient) {
        if (autoPull.getAsBoolean() || syncManager == null) return;
        FluidUtil.getFluidContained(ingredient).ifPresent(fluid -> {
            if (!fluid.isEmpty()) {
                syncManager.callSyncedAction(SET_GHOST, buf -> {
                    buf.writeVarInt(findTargetSlot());
                    buf.writeBoolean(true);
                    fluid.writeToPacket(buf);
                });
            }
        });
    }

    @Override
    public boolean ingredientHandlingOverride(Object ingredient) {
        if (autoPull.getAsBoolean() || syncManager == null) return false;
        if (!(ingredient instanceof EmiStack emiStack)) return false;
        FluidStack fluidStack = EmiStackConverter.FLUID.convertFrom(emiStack);
        if (fluidStack == null) return false;
        if (fluidStack.getAmount() <= 0) fluidStack.setAmount(1000);
        int slot = findTargetSlot();
        if (slot < 0) return false;
        syncManager.callSyncedAction(SET_GHOST, buf -> {
            buf.writeVarInt(slot);
            buf.writeBoolean(true);
            fluidStack.writeToPacket(buf);
        });
        return true;
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(Object ingredient) {
        return null;
    }

    @Override
    public Class<ItemStack> ingredientClass() {
        return ItemStack.class;
    }

    // --- Internals ---

    private int findTargetSlot() {
        int hovered = getSlotAtLocal(lastMouseX, lastMouseY);
        if (hovered >= 0) return hovered;
        for (int i = 0; i < slotCount; i++) {
            if (configSyncHandler == null || configSyncHandler.getClientConfig(i) == null) return i;
        }
        return 0;
    }

    private int getSlotAtLocal(double localX, double localY) {
        for (int i = 0; i < slotCount; i++) {
            int x = slotX(i);
            int y = slotY(i);
            if (localX >= x && localX < x + CELL_SIZE && localY >= y && localY < y + CELL_SIZE * 2) {
                return i;
            }
        }
        return -1;
    }
}
