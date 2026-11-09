package com.yiran.minecraft.gtmqol.common.crafter;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.ObjectValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ItemDisplayWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * The pattern items: right click opens an encoding window like AE2's pattern encoding terminal, with a button for the
 * kind of recipe (crafting, stonecutting, smithing) and a grid of ghost items (dragged from the recipe viewer, or
 * filled by its transfer button) with the recipe's result next to it. An item can be stonecut into several things:
 * clicking the result goes through them. A stack of blank patterns gives one encoded pattern per Encode.
 */
final class CraftingPatternBehavior implements IItemUIHolder, IAddInformation {

    private static final String PREFIX = "gtmqol.crafting_pattern.";
    private static final String TITLE_KEY = PREFIX + "title";
    private static final String ENCODE_KEY = PREFIX + "encode";
    private static final String CLEAR_KEY = PREFIX + "clear";
    private static final String CLEAR_TOOLTIP_KEY = PREFIX + "clear.tooltip";
    private static final String FLUIDS_ON_KEY = PREFIX + "fluids.on";
    private static final String FLUIDS_OFF_KEY = PREFIX + "fluids.off";
    private static final String SUBSTITUTES_ON_KEY = PREFIX + "substitutes.on";
    private static final String SUBSTITUTES_OFF_KEY = PREFIX + "substitutes.off";
    private static final String DUPLICATES_ON_KEY = PREFIX + "duplicates.on";
    private static final String DUPLICATES_OFF_KEY = PREFIX + "duplicates.off";
    private static final String MODE_KEY = PREFIX + "mode.";
    private static final String NEXT_RESULT_KEY = PREFIX + "next_result";
    private static final String EMPTY_KEY = PREFIX + "status.empty";
    private static final String NO_RECIPE_KEY = PREFIX + "status.no_recipe";
    private static final String KEPT_LEGEND_KEY = PREFIX + "status.kept";
    private static final String FLUID_LEGEND_KEY = PREFIX + "status.fluid";
    private static final String CHANGED_LEGEND_KEY = PREFIX + "status.changed";
    private static final String BLANK_KEY = PREFIX + "tooltip.blank";
    private static final String BROKEN_KEY = PREFIX + "tooltip.broken";
    private static final String CRAFTS_KEY = PREFIX + "tooltip.crafts";
    private static final String GIVES_BACK_KEY = PREFIX + "tooltip.gives_back";
    private static final String USES_KEY = PREFIX + "tooltip.uses";
    private static final String KEEPS_KEY = PREFIX + "tooltip.keeps";
    private static final String USES_FLUID_KEY = PREFIX + "tooltip.uses_fluid";
    private static final String WEARS_KEY = PREFIX + "tooltip.wears";
    private static final String SUBSTITUTES_KEY = PREFIX + "tooltip.substitutes";

    // no cornerRadius: MUI 1.21 drawRoundedRect misses the pose on the bottom left corner
    private static final IDrawable KEPT_TINT = new Rectangle().color(0x6040C040);
    private static final IDrawable FLUID_TINT = new Rectangle().color(0x604080F0);
    private static final IDrawable CHANGED_TINT = new Rectangle().color(0x60E0C040);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 196;

    /** The window's panel; the screen fills the grid through it. */
    static final class EncodingPanel extends ModularPanel<EncodingPanel> {

        private final PanelSyncManager syncManager;
        private final Level level;

        EncodingPanel(PanelSyncManager syncManager, Level level) {
            super("crafting_pattern");
            this.syncManager = syncManager;
            this.level = level;
        }

        /** Puts a recipe on the grid. With choices in its slots, substitutes go on: else it would take only the first. */
        void fill(Recipe<?> recipe) {
            CraftingPattern.Mode mode = CraftingPattern.Mode.of(recipe);
            if (mode == null) return;
            List<ItemStack> grid = CraftingPattern.grid(recipe);
            int choice = CraftingPattern.choice(recipe.getId(), grid, level);
            syncManager.callSyncedAction("fill", buf -> {
                buf.writeEnum(mode);
                for (ItemStack item : grid) buf.writeItem(item);
                buf.writeVarInt(choice);
                buf.writeBoolean(CraftingPattern.hasChoices(recipe));
            });
        }
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        Player player = data.getPlayer();
        Level level = player.level();
        var grid = new CustomItemStackHandler(CraftingPattern.SLOTS) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        CraftingPattern.Mode[] mode = { CraftingPattern.Mode.CRAFTING };
        int[] choice = { 0 };
        boolean[] fluids = { true };
        boolean[] substitutes = { false };
        boolean[] duplicates = { false };
        CraftingPattern current = CraftingPattern.read(data.getUsedItemStack());
        if (current != null) {
            mode[0] = current.mode();
            choice[0] = CraftingPattern.choice(current.recipeId(), current.inputs(), level);
            fluids[0] = current.fluids();
            substitutes[0] = current.substitutes();
            duplicates[0] = current.duplicates();
            for (int slot = 0; slot < CraftingPattern.SLOTS; slot++) {
                grid.setStackInSlot(slot, current.inputs().get(slot).copy());
            }
        }
        // grid changes on the server go through the slots, as clicks do, so the client hears of them
        var slots = new PhantomItemSlotSyncHandler[CraftingPattern.SLOTS];
        for (int slot = 0; slot < CraftingPattern.SLOTS; slot++) {
            slots[slot] = new PhantomItemSlotSyncHandler(new ModularSlot(grid, slot).accessibility(true, false));
        }
        var preview = new Preview(grid, mode, choice, fluids, duplicates, level);
        var modeValue = new EnumSyncValue<>(CraftingPattern.Mode.class, () -> mode[0], value -> {
            mode[0] = value;
            choice[0] = 0;
            for (int slot = 0; slot < CraftingPattern.SLOTS; slot++) {
                if (!value.uses(slot) && !level.isClientSide) slots[slot].getSlot().set(ItemStack.EMPTY);
            }
        }).allowC2S();
        var choiceValue = new IntSyncValue(() -> choice[0], value -> choice[0] = value).allowC2S();
        syncManager.syncValue("choice", choiceValue);
        var fluidsValue = new BooleanSyncValue(() -> fluids[0], value -> fluids[0] = value).allowC2S();
        var substitutesValue = new BooleanSyncValue(() -> substitutes[0], value -> substitutes[0] = value).allowC2S();
        var duplicatesValue = new BooleanSyncValue(() -> duplicates[0], value -> duplicates[0] = value).allowC2S();

        syncManager.registerSyncedAction("encode", false, true, buf -> {
            CraftingPattern pattern = CraftingPattern.encode(mode[0], items(grid), choice[0], fluids[0],
                    substitutes[0], duplicates[0], level);
            if (pattern == null) return;
            ItemStack held = data.getUsedItemStack();
            if (held.is(PatternCrafter.PATTERN.get())) {
                pattern.write(held);
                return;
            }
            ItemStack encoded = PatternCrafter.PATTERN.asStack();
            pattern.write(encoded);
            held.shrink(1);
            if (held.isEmpty()) data.setUsedItemStack(encoded);
            else if (!player.addItem(encoded)) player.drop(encoded, false);
        });
        // an encoded pattern goes back to blank too, else its recipe is back the next time it is opened
        syncManager.registerSyncedAction("clear", false, true, buf -> {
            for (var slot : slots) slot.getSlot().set(ItemStack.EMPTY);
            if (data.getUsedItemStack().is(PatternCrafter.PATTERN.get())) {
                data.setUsedItemStack(PatternCrafter.BLANK_PATTERN.asStack());
            }
        });

        syncManager.registerSyncedAction("fill", false, true, buf -> {
            mode[0] = buf.readEnum(CraftingPattern.Mode.class);
            for (int slot = 0; slot < CraftingPattern.SLOTS; slot++) {
                slots[slot].getSlot().set(buf.readItem());
            }
            choice[0] = buf.readVarInt();
            substitutes[0] = buf.readBoolean();
        });

        var panel = new EncodingPanel(syncManager, level);
        return panel.size(WIDTH, HEIGHT)
                .background(GTGuiTextures.BACKGROUND)
                .child(Flow.col()
                        .top(6)
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .childPadding(4)
                        .child(Text.lang(TITLE_KEY).asWidget())
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(8)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .child(new Grid()
                                        .coverChildren()
                                        .gridOfSizeWidth(CraftingPattern.SLOTS, CraftingPattern.SIZE,
                                                (x, y, slot) -> new PhantomItemSlot()
                                                        .background(GTGuiTextures.SLOT, new DynamicDrawable(
                                                                () -> tint(preview.use(slot))))
                                                        .setEnabledIf(widget -> mode[0].uses(slot))
                                                        .syncHandler(slots[slot])))
                                .child(Text.str("→").asWidget())
                                .child(new ParentWidget<>()
                                        .size(18)
                                        .child(new ItemDisplayWidget()
                                                .item(new ObjectValue.Dynamic<>(ItemStack.class, preview::result,
                                                        item -> {}))
                                                .displayAmount(true)
                                                .background(GTGuiTextures.SLOT)
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> {
                                                    if (!preview.result().isEmpty()) {
                                                        tooltip.addFromItem(preview.result());
                                                    }
                                                }))
                                        // over the result, for stonecutting: clicking it goes to the item's next recipe
                                        .child(new ButtonWidget<>()
                                                .size(18)
                                                .background(IDrawable.EMPTY)
                                                .hoverBackground(IDrawable.EMPTY)
                                                .setEnabledIf(button -> preview.choices() > 1)
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> {
                                                    if (!preview.result().isEmpty()) {
                                                        tooltip.addFromItem(preview.result());
                                                    }
                                                })
                                                .onMousePressed((context, button) -> {
                                                    choiceValue.setIntValue(
                                                            (choice[0] + 1) % preview.choices(), true, true);
                                                    return true;
                                                })))
                                .child(Flow.col()
                                        .coverChildren()
                                        .child(new CycleButtonWidget()
                                                .size(18)
                                                .value(modeValue)
                                                .stateCount(CraftingPattern.Mode.values().length)
                                                .stateOverlay(CraftingPattern.Mode.CRAFTING,
                                                        new ItemDrawable(Items.CRAFTING_TABLE).asIcon().size(16))
                                                .stateOverlay(CraftingPattern.Mode.STONECUTTING,
                                                        new ItemDrawable(Items.STONECUTTER).asIcon().size(16))
                                                .stateOverlay(CraftingPattern.Mode.SMITHING,
                                                        new ItemDrawable(Items.SMITHING_TABLE).asIcon().size(16))
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(MODE_KEY +
                                                        modeValue.getValue().name().toLowerCase(Locale.ROOT)))))
                                        .child(new ToggleButton()
                                                .size(18)
                                                .setEnabledIf(button -> mode[0] == CraftingPattern.Mode.CRAFTING)
                                                .value(fluidsValue)
                                                .overlay(new ItemDrawable(Items.WATER_BUCKET).asIcon().size(16))
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(
                                                        fluidsValue.getBoolValue() ? FLUIDS_ON_KEY :
                                                                FLUIDS_OFF_KEY)))))
                                .child(Flow.col()
                                        .coverChildren()
                                        .child(new ToggleButton()
                                                .size(18)
                                                .setEnabledIf(button -> mode[0] == CraftingPattern.Mode.CRAFTING ||
                                                        mode[0] == CraftingPattern.Mode.SMITHING)
                                                .value(substitutesValue)
                                                .overlay(new ItemDrawable(Items.OAK_PLANKS).asIcon().size(16))
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(
                                                        substitutesValue.getBoolValue() ? SUBSTITUTES_ON_KEY :
                                                                SUBSTITUTES_OFF_KEY))))
                                        .child(new ToggleButton()
                                                .size(18)
                                                .setEnabledIf(button -> mode[0] == CraftingPattern.Mode.CRAFTING)
                                                .value(duplicatesValue)
                                                .overlay(new ItemDrawable(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                                                        .asIcon().size(16))
                                                .tooltipAutoUpdate(true)
                                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(
                                                        duplicatesValue.getBoolValue() ? DUPLICATES_ON_KEY :
                                                                DUPLICATES_OFF_KEY))))))
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(6)
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(ENCODE_KEY))
                                        .onMousePressed((context, button) -> {
                                            if (preview.plan() == null) return false;
                                            syncManager.callSyncedAction("encode");
                                            panel.closeIfOpen();
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(CLEAR_KEY))
                                        .tooltip(new RichTooltip().addLine(Component.translatable(CLEAR_TOOLTIP_KEY)))
                                        .onMousePressed((context, button) -> {
                                            syncManager.callSyncedAction("clear");
                                            return true;
                                        })))
                        .child(new TextWidget<>(Text.dynamic(preview::status)).maxWidth(WIDTH - 12)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7).disableSortButtons());
    }

    private static List<ItemStack> items(CustomItemStackHandler grid) {
        return IntStream.range(0, CraftingPattern.SLOTS).mapToObj(grid::getStackInSlot).toList();
    }

    private static IDrawable tint(CraftingPattern.SlotUse use) {
        return switch (use) {
            case KEPT -> KEPT_TINT;
            case FLUID -> FLUID_TINT;
            case CHANGED -> CHANGED_TINT;
            default -> IDrawable.EMPTY;
        };
    }

    /** What the grid would encode to, worked out again only when the grid or the fluid button changes. */
    private static final class Preview {

        private final CustomItemStackHandler grid;
        private final CraftingPattern.Mode[] mode;
        private final int[] choice;
        private final boolean[] fluids;
        private final boolean[] duplicates;
        private final Level level;
        private @Nullable List<ItemStack> seenGrid;
        private CraftingPattern.@Nullable Mode seenMode;
        private int seenChoice;
        private boolean seenFluids;
        private boolean seenDuplicates;
        private CraftingPattern.@Nullable Plan plan;
        private int choices;

        Preview(CustomItemStackHandler grid, CraftingPattern.Mode[] mode, int[] choice, boolean[] fluids,
                boolean[] duplicates, Level level) {
            this.grid = grid;
            this.mode = mode;
            this.choice = choice;
            this.fluids = fluids;
            this.duplicates = duplicates;
            this.level = level;
        }

        CraftingPattern.@Nullable Plan plan() {
            List<ItemStack> items = items(grid);
            if (seenGrid == null || seenMode != mode[0] || seenChoice != choice[0] || seenFluids != fluids[0] ||
                    seenDuplicates != duplicates[0] || !same(items, seenGrid)) {
                seenGrid = items.stream().map(ItemStack::copy).toList();
                seenMode = mode[0];
                seenChoice = choice[0];
                seenFluids = fluids[0];
                seenDuplicates = duplicates[0];
                choices = mode[0] == CraftingPattern.Mode.STONECUTTING ?
                        CraftingPattern.stonecutting(items, level).size() : 0;
                // what each slot does doesn't depend on substitutes
                CraftingPattern pattern = CraftingPattern.encode(mode[0], items, choice[0], fluids[0], false,
                        duplicates[0], level);
                plan = pattern == null ? null : pattern.plan(level);
            }
            return plan;
        }

        private static boolean same(List<ItemStack> a, List<ItemStack> b) {
            return IntStream.range(0, CraftingPattern.SLOTS).allMatch(i -> ItemStack.matches(a.get(i), b.get(i)));
        }

        /** How many recipes the stonecutting input has to pick from. */
        int choices() {
            plan();
            return choices;
        }

        ItemStack result() {
            var current = plan();
            return current == null ? ItemStack.EMPTY : current.outputs().get(0);
        }

        CraftingPattern.SlotUse use(int slot) {
            var current = plan();
            return current == null ? CraftingPattern.SlotUse.EMPTY : current.uses().get(slot);
        }

        Component status() {
            var current = plan();
            if (current == null) {
                if (items(grid).stream().allMatch(ItemStack::isEmpty)) {
                    return Component.translatable(EMPTY_KEY).withStyle(ChatFormatting.DARK_GRAY);
                }
                return Component.translatable(NO_RECIPE_KEY).withStyle(ChatFormatting.RED);
            }
            if (choices() > 1) {
                return Component.translatable(NEXT_RESULT_KEY, Math.floorMod(choice[0], choices) + 1, choices)
                        .withStyle(ChatFormatting.DARK_GRAY);
            }
            MutableComponent legend = Component.empty();
            boolean kept = current.uses().contains(CraftingPattern.SlotUse.KEPT);
            boolean changed = current.uses().contains(CraftingPattern.SlotUse.CHANGED);
            if (kept) legend.append(Component.translatable(KEPT_LEGEND_KEY).withStyle(ChatFormatting.DARK_GREEN));
            if (changed) {
                if (kept) legend.append(Component.literal("  "));
                legend.append(Component.translatable(CHANGED_LEGEND_KEY).withStyle(ChatFormatting.GOLD));
            }
            if (!current.fluids().isEmpty()) {
                if (kept || changed) legend.append(Component.literal("  "));
                legend.append(Component.translatable(FLUID_LEGEND_KEY).withStyle(ChatFormatting.BLUE));
            }
            return legend;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag isAdvanced) {
        CraftingPattern pattern = CraftingPattern.read(stack);
        if (pattern == null) {
            tooltip.add(Component.translatable(BLANK_KEY));
            return;
        }
        // drawn every frame while hovered, so from the cache
        var plan = level != null && ICrafterPattern.decode(stack, level) instanceof CraftingPattern.Plan found ? found : null;
        if (plan == null) {
            tooltip.add(Component.translatable(BROKEN_KEY).withStyle(ChatFormatting.RED));
            return;
        }
        for (int i = 0; i < plan.outputs().size(); i++) {
            ItemStack output = plan.outputs().get(i);
            tooltip.add(Component.translatable(i == 0 ? CRAFTS_KEY : GIVES_BACK_KEY,
                    output.getCount(), output.getHoverName()));
        }
        for (var input : plan.consumed()) {
            tooltip.add(Component.translatable(USES_KEY, input.item().getCount(), input.item().getHoverName()));
        }
        for (var kept : plan.kept()) {
            boolean wears = kept.use() == CraftingPattern.SlotUse.CHANGED;
            tooltip.add(Component.translatable(wears ? WEARS_KEY : KEEPS_KEY, kept.item().getHoverName()));
        }
        for (FluidStack fluid : plan.fluids()) {
            tooltip.add(Component.translatable(USES_FLUID_KEY, fluid.getAmount(), fluid.getDisplayName()));
        }
        if (pattern.substitutes()) tooltip.add(Component.translatable(SUBSTITUTES_KEY));
    }

    static void addLang() {
        addLang(TITLE_KEY, "Crafting Pattern");
        addLang(ENCODE_KEY, "Encode");
        addLang(CLEAR_KEY, "Clear");
        addLang(CLEAR_TOOLTIP_KEY, "Empties the grid; an encoded pattern goes back to blank");
        addLang(FLUIDS_ON_KEY, "Buckets are taken as fluid from the tanks");
        addLang(FLUIDS_OFF_KEY, "Buckets are taken as items");
        addLang(SUBSTITUTES_ON_KEY, "Accepts anything the recipe accepts, at any durability (smithing: the " +
                "template and the addition)");
        addLang(SUBSTITUTES_OFF_KEY, "Accepts only these exact items, durability included");
        addLang(DUPLICATES_ON_KEY, "An item the recipe makes more of is kept, and only what it adds comes out");
        addLang(DUPLICATES_OFF_KEY, "Recipes that make more of an input use it up as usual");
        addLang(MODE_KEY + "crafting", "Crafting table recipe");
        addLang(MODE_KEY + "stonecutting", "Stonecutter recipe");
        addLang(MODE_KEY + "smithing", "Smithing table recipe: template, item, material");
        addLang(NEXT_RESULT_KEY, "Result %s of %s: click it for the next");
        addLang(EMPTY_KEY, "Place a recipe in the grid");
        addLang(NO_RECIPE_KEY, "No recipe matches");
        addLang(KEPT_LEGEND_KEY, "Green: not consumed");
        addLang(FLUID_LEGEND_KEY, "Blue: fluid");
        addLang(CHANGED_LEGEND_KEY, "Yellow: wears out");
        addLang(BLANK_KEY, "Right click to encode a recipe");
        addLang(BROKEN_KEY, "Its recipe changed or no longer exists");
        addLang(CRAFTS_KEY, "Crafts %sx %s");
        addLang(GIVES_BACK_KEY, "Gives back %sx %s");
        addLang(USES_KEY, "Uses %sx %s");
        addLang(KEEPS_KEY, "Needs %s, not consumed");
        addLang(USES_FLUID_KEY, "Uses %s mB of %s");
        addLang(WEARS_KEY, "Needs %s, which wears out");
        addLang(SUBSTITUTES_KEY, "Accepts substitutes");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
