package com.yiran.minecraft.gtmqol.common.steam;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

/** Sets the parallels of an {@link AdvancedSteamMultiMachine}, 16 to 256. UI is GTCEu's parallel hatch, simplified. */
public class SteamParallelHatchPartMachine extends MultiblockPartMachine implements IMuiMachine {

    public static final int MIN_PARALLEL = AdvancedSteamMultiMachine.DEFAULT_PARALLELS;
    public static final int MAX_PARALLEL = 256;

    @SaveField
    private int currentParallel = MAX_PARALLEL;

    public SteamParallelHatchPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    public int getCurrentParallel() {
        return currentParallel;
    }

    public void setCurrentParallel(int parallelAmount) {
        this.currentParallel = Mth.clamp(parallelAmount, MIN_PARALLEL, MAX_PARALLEL);
        for (MultiblockControllerMachine controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    @Override
    public boolean canShared(MultiblockControllerMachine controller, String substructureName) {
        return false;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        IntSyncValue parallels = new IntSyncValue(this::getCurrentParallel, this::setCurrentParallel).allowC2S();
        mainWidget.child(Flow.row()
                .size(180, 60)
                .child(new ButtonWidget<>()
                        .overlay(Text.str("/2"))
                        .width(32)
                        .height(16)
                        .onMousePressed((context, button) -> {
                            parallels.setValue(Mth.clamp(parallels.getValue() / 2, MIN_PARALLEL, MAX_PARALLEL));
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(new TextFieldWidget()
                        .width(40)
                        .setTextAlignment(Alignment.CENTER)
                        .setNumbers(MIN_PARALLEL, MAX_PARALLEL)
                        .value(parallels)
                        .setDefaultNumber(MIN_PARALLEL)
                        .marginLeft(4)
                        .verticalCenter())
                .child(new ButtonWidget<>()
                        .overlay(Text.str("x2"))
                        .width(32)
                        .height(16)
                        .onMousePressed((context, button) -> {
                            parallels.setValue(Mth.clamp(parallels.getValue() * 2, MIN_PARALLEL, MAX_PARALLEL));
                            return true;
                        })
                        .marginLeft(4)
                        .verticalCenter())
                .child(Text.lang("gtceu.machine.parallel_hatch.parallel_ui")
                        .asWidget()
                        .marginLeft(4)
                        .marginRight(4)
                        .verticalCenter()));
    }
}
