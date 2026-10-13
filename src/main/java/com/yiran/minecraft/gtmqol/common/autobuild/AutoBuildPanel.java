package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.text.DynamicComponent;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.IntIntPair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The right-click settings window of the auto-build button. Client only and not synced: it edits the client's
 * {@link BuildSettings} for the multiblock type, which is sent along with every click.
 */
public final class AutoBuildPanel {

    private static final String KEY = "gtmqol.auto_build.settings.";
    public static final String TITLE_KEY = KEY + "title";
    public static final String HINT_KEY = KEY + "hint";
    public static final String LAYER_KEY = KEY + "layer";
    public static final String SIZE_KEY = KEY + "size";
    public static final String BLOCKS_KEY = KEY + "blocks";
    public static final String RESET_KEY = KEY + "reset";
    public static final String NOTHING_KEY = KEY + "nothing";

    private static final int WIDTH = 180;
    private static final int MAX_LIST_HEIGHT = 200;
    private static final int ROW = 18;
    private static final int LABEL = 12;
    private static final int PER_ROW = 9;

    /** Transient: per multiblock type, for this game session. */
    private static final Map<ResourceLocation, BuildSettings> SETTINGS = new HashMap<>();

    private AutoBuildPanel() {}

    public static BuildSettings settings(MultiblockControllerMachine controller) {
        return SETTINGS.computeIfAbsent(controller.getDefinition().getId(), id -> BuildSettings.create());
    }

    public static ModularPanel<?> build(ModularPanel<?> parent, MultiblockControllerMachine controller) {
        IBlockPattern pattern = controller.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE);
        BuildOptions options = pattern == null ? new BuildOptions(List.of(), List.of(), List.of()) :
                BuildOptions.of(pattern);
        BuildSettings settings = settings(controller);

        ListWidget<IWidget, ?> list = new ListWidget<>();
        int height = 0;
        if (options.isEmpty()) {
            list.child(Text.lang(NOTHING_KEY).asWidget().height(LABEL));
            height += LABEL;
        }
        for (int i = 0; i < options.slices().size(); i++) {
            list.child(sliceRow(i, options.slices().get(i), settings));
            height += ROW;
        }
        for (int i = 0; i < options.bounds().size(); i++) {
            IntIntPair bounds = options.bounds().get(i);
            if (bounds.leftInt() >= bounds.rightInt()) continue;
            list.child(sizeRow(i, options.bounds(), settings));
            height += ROW;
        }
        for (int i = 0; i < options.choices().size(); i++) {
            BuildOptions.ChoicePoint point = options.choices().get(i);
            list.child(Text.lang(BLOCKS_KEY, i + 1).asWidget().height(LABEL));
            height += LABEL;
            List<BuildOptions.Option> choices = point.options();
            for (int start = 0; start < choices.size(); start += PER_ROW) {
                Flow row = Flow.row().height(ROW).widthRel(1f).childPadding(2);
                for (var option : choices.subList(start, Math.min(choices.size(), start + PER_ROW))) {
                    row.child(blockToggle(point.symbol(), option, settings));
                }
                list.child(row);
                height += ROW;
            }
        }
        int listHeight = Math.min(height, MAX_LIST_HEIGHT);

        return ModularPanel.defaultPanel("gtmqol_auto_build_settings", WIDTH, listHeight + 46)
                .relative(parent).top(0).rightRel(1f)
                .draggable(true)
                .child(ButtonWidget.panelCloseButton())
                .child(Text.lang(TITLE_KEY).asWidget().pos(7, 7))
                .child(Text.lang(HINT_KEY).asWidget().pos(7, 19).scale(0.75f))
                .child(list.pos(7, 30).size(WIDTH - 14, listHeight))
                .child(new ButtonWidget<>()
                        .pos(7, listHeight + 32).size(50, 12)
                        .overlay(Text.lang(RESET_KEY))
                        .onMousePressed((context, button) -> {
                            settings.sliceRepeats().clear();
                            settings.dimensions().clear();
                            settings.blocks().clear();
                            return true;
                        }));
    }

    private static IWidget sliceRow(int number, BuildOptions.Slice slice, BuildSettings settings) {
        IntValue.Dynamic value = new IntValue.Dynamic(
                () -> settings.sliceRepeats().getOrDefault(slice.index(), slice.min()),
                v -> settings.sliceRepeats().put(slice.index(), v));
        return sliderRow(Text.dynamic(() -> Component.translatable(LAYER_KEY, number + 1, value.getIntValue())),
                slice.min(), slice.max(), value);
    }

    private static IWidget sizeRow(int index, List<IntIntPair> bounds, BuildSettings settings) {
        IntIntPair minMax = bounds.get(index);
        IntValue.Dynamic value = new IntValue.Dynamic(
                () -> settings.dimensions().size() == bounds.size() ? settings.dimensions().getInt(index) :
                        minMax.leftInt(),
                v -> {
                    // the planner only uses the list when it has every dimension
                    if (settings.dimensions().size() != bounds.size()) {
                        settings.dimensions().clear();
                        bounds.forEach(b -> settings.dimensions().add(b.leftInt()));
                    }
                    settings.dimensions().set(index, v);
                });
        return sliderRow(Text.dynamic(() -> Component.translatable(SIZE_KEY, index + 1, value.getIntValue())),
                minMax.leftInt(), minMax.rightInt(), value);
    }

    private static IWidget sliderRow(DynamicComponent label, int min, int max, IntValue.Dynamic value) {
        return Flow.row().height(ROW).widthRel(1f).childPadding(4)
                .child(label.asWidget().width(70).height(16))
                .child(new SliderWidget()
                        .background(GTGuiTextures.FLUID_SLOT)
                        .bounds(min, max).stopper(1.0)
                        .value(value)
                        .width(WIDTH - 14 - 74 - 6).height(16));
    }

    /** Selected again turns it off, back to the pattern's default. */
    private static IWidget blockToggle(char symbol, BuildOptions.Option option, BuildSettings settings) {
        var choice = new BuildSettings.BlockChoice(option.index(), option.block());
        ItemStack stack = new ItemStack(option.block());
        return new ToggleButton()
                .size(16)
                .overlay(new ItemDrawable(stack))
                .value(new BoolValue.Dynamic(() -> choice.equals(settings.blocks().get(symbol)), selected -> {
                    if (selected) settings.blocks().put(symbol, choice);
                    else settings.blocks().remove(symbol);
                }))
                .tooltipStatic(t -> t.addLine(stack.getHoverName()));
    }
}
