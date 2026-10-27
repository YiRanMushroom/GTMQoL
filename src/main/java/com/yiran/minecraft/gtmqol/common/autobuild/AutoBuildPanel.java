package com.yiran.minecraft.gtmqol.common.autobuild;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.MultiblockPreviewWidget;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The right-click settings window of the auto-build button: GTCEu's multiblock preview (the terminal's, without its
 * terminal buttons), turned like the controller. Client only and not synced: it edits the client's
 * {@link MultiblockSchemaInfo} for the multiblock type, which is sent as {@link BuildSettings} with every click.
 * <p>
 * Positions picked in the preview are in the controller's layout, so they are only kept for that controller (same
 * position and facing, preview not flipped); the rest holds for every controller of the type.
 */
public final class AutoBuildPanel {

    private static final String KEY = "gtmqol.auto_build.settings.";
    public static final String TITLE_KEY = KEY + "title";
    public static final String RESET_KEY = KEY + "reset";

    private static final int PREVIEW_SIZE = 200;

    /** The controller (position and facing) the picked positions belong to. */
    private record Placement(BlockPos pos, Direction front, Direction up, boolean flipped) {

        static Placement of(MultiblockControllerMachine controller) {
            return new Placement(controller.getBlockPos(), controller.getFrontFacing(),
                    controller.getUpwardsFacing(), controller.isFlipped());
        }
    }

    private static final class Stored {

        MultiblockSchemaInfo info;
        @Nullable
        Placement placement;

        Stored(MultiblockControllerMachine controller) {
            info = new MultiblockSchemaInfo(controller.getDefinition());
        }
    }

    /** Transient: per multiblock type, for this game session. */
    private static final Map<ResourceLocation, Stored> SETTINGS = new HashMap<>();

    private AutoBuildPanel() {}

    private static Stored stored(MultiblockControllerMachine controller) {
        Stored stored = SETTINGS.computeIfAbsent(controller.getDefinition().getId(), id -> new Stored(controller));
        Placement placement = Placement.of(controller);
        if (!placement.equals(stored.placement)) {
            stored.info.getUserGlobalBlockPreferences().clear();
            stored.placement = placement;
        }
        return stored;
    }

    public static BuildSettings settings(MultiblockControllerMachine controller) {
        MultiblockSchemaInfo info = stored(controller).info;
        BuildSettings settings = BuildSettings.create();
        settings.sliceRepeats().putAll(info.getUserSliceRepeats());
        settings.dimensions().addAll(info.getUserDimensions());
        for (var entry : info.getBlockPreferences().char2ObjectEntrySet()) {
            settings.blocks().put(entry.getCharKey(), entry.getValue().getBlockState());
        }
        info.getUserGlobalBlockPreferences().forEach((pos, block) -> settings.positions().put(pos,
                block.getBlockState()));
        return settings;
    }

    public static ModularPanel<?> build(MultiblockControllerMachine controller) {
        Stored stored = stored(controller);
        MultiblockPreviewWidget preview = new MultiblockPreviewWidget(controller.getDefinition(), stored.info,
                PREVIEW_SIZE, PREVIEW_SIZE, false)
                .setControllerPos(controller.getBlockPos())
                .setFrontFacing(controller.getFrontFacing())
                .setUpFacing(controller.getUpwardsFacing())
                .setFlipped(controller.isFlipped());
        preview.refreshSchema();

        // centered on the screen (ModularPanel's default)
        ModularPanel<?> panel = ModularPanel.defaultPanel("gtmqol_auto_build_settings").coverChildren();
        return panel
                .child(Text.lang(TITLE_KEY).asWidget().pos(8, 8))
                .child(new ButtonWidget<>()
                        .top(5).right(7).size(40, 14)
                        .overlay(Text.lang(RESET_KEY))
                        .onMousePressed((context, button) -> {
                            SETTINGS.remove(controller.getDefinition().getId());
                            panel.closeIfOpen();
                            return true;
                        }))
                // Untested: GTCEu's preview inside our machine UI panel (it's built for the terminal's own screen)
                .child(preview.top(20))
                .onCloseAction(closed -> {
                    // the preview's flip button changes the layout the picked positions refer to
                    if (preview.isFlipped() != controller.isFlipped()) {
                        stored.info.getUserGlobalBlockPreferences().clear();
                    }
                });
    }
}
