package com.yiran.minecraft.gtmqol.integration.ae2.matrix;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.BatchCraftingProvider;
import com.yiran.minecraft.gtmqol.integration.ae2.batch.PatternBatch;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternContainer;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * The assembling matrix on the ME network: offers its patterns, takes the crafts, hands the products over. Its window
 * names the matrix in pattern terminals and switches the controller's auto upload.
 */
public class AssemblingMatrixInterfaceMachine extends MultiblockPartMachine
                                    implements IGridConnectedMachine, ICraftingProvider, BatchCraftingProvider,
                                    PatternContainer, IMuiMachine {

    private static final String KEY = AssemblingMatrixMachine.KEY + "interface.";
    private static final String RENAME_PANEL = "matrix_interface_rename";

    @SaveField
    private final GridNodeHolder nodeHolder;
    @SaveField
    @SyncToClient
    private String customName = "";
    private final IActionSource source;
    private List<IPatternDetails> patterns = List.of();
    private Set<IPatternDetails> offered = Set.of();
    private boolean online;

    public AssemblingMatrixInterfaceMachine(BlockEntityCreationInfo info) {
        super(info);
        this.nodeHolder = attachTrait(new GridNodeHolder(this));
        this.source = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
        nodeHolder.getMainNode()
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .addService(ICraftingProvider.class, this);
    }

    private @Nullable AssemblingMatrixMachine matrix() {
        for (MultiblockControllerMachine controller : getControllers()) {
            if (controller instanceof AssemblingMatrixMachine matrix && matrix.isFormed()) return matrix;
        }
        return null;
    }

    void updatePatterns(List<IPatternDetails> patterns) {
        this.patterns = patterns;
        this.offered = new HashSet<>(patterns);
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void removedFromController(MultiblockControllerMachine controller) {
        super.removedFromController(controller);
        updatePatterns(List.of());
    }

    /** Where encoded patterns go from a terminal, when the matrix wants them. */
    public @Nullable InternalInventory uploadInventory() {
        AssemblingMatrixMachine matrix = matrix();
        return matrix != null && matrix.isAutoUpload() ? matrix.terminalInventory() : null;
    }

    /** What the ME network took of it. */
    long insert(AEKey what, long amount) {
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !getMainNode().isActive()) return 0;
        return grid.getStorageService().getInventory().insert(what, amount, Actionable.MODULATE, source);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patterns;
    }

    /** A catalyst that wears out has to be found in working order by each craft. */
    @Override
    public long batchLimit(IPatternDetails pattern) {
        return pattern instanceof CraftingPatternDetails details && details.wearsCatalyst() ? 0 : Long.MAX_VALUE;
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputs) {
        AssemblingMatrixMachine matrix = matrix();
        if (matrix == null || !getMainNode().isActive()) return false;
        IPatternDetails pattern = PatternBatch.unwrap(details);
        long crafts = details instanceof PatternBatch scaled ? scaled.multiplier() : 1;
        return offered.contains(pattern) && matrix.push(pattern, inputs, crafts);
    }

    @Override
    public boolean isBusy() {
        return matrix() == null;
    }

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public boolean isVisibleInTerminal() {
        return matrix() != null;
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        AssemblingMatrixMachine matrix = matrix();
        return matrix == null ? InternalInventory.empty() : matrix.terminalInventory();
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return new PatternContainerGroup(AEItemKey.of(AssemblingMatrix.CONTROLLER.asStack()), name(), List.of());
    }

    private Component name() {
        return name(customName);
    }

    private static Component name(String customName) {
        return customName.isEmpty() ? Component.translatable(AssemblingMatrix.CONTROLLER.getDescriptionId()) :
                Component.literal(customName);
    }

    private void setCustomName(String name) {
        customName = name.strip();
        getSyncDataHolder().markClientSyncFieldDirty("customName");
        markAsChanged();
    }

    private boolean isAutoUpload() {
        AssemblingMatrixMachine matrix = matrix();
        return matrix != null && matrix.isAutoUpload();
    }

    private void setAutoUpload(boolean autoUpload) {
        AssemblingMatrixMachine matrix = matrix();
        if (matrix != null) matrix.setAutoUpload(autoUpload);
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        var connected = new BooleanSyncValue(() -> matrix() != null);
        var online = new BooleanSyncValue(this::isOnline);
        var shownName = new StringSyncValue(() -> customName);
        syncManager.syncValue("connected", connected);
        syncManager.syncValue("online", online);
        syncManager.syncValue("name", shownName);
        IPanelHandler renaming = syncManager.syncedPanel(RENAME_PANEL, true,
                (manager, handler) -> popup(RENAME_PANEL, 196, 48)
                        .child(Flow.column()
                                .padding(6)
                                .childPadding(4)
                                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                                .child(Text.lang(KEY + "rename").asWidget())
                                .child(new TextFieldWidget()
                                        .size(180, 14)
                                        .autoUpdateOnChange(true)
                                        .value(SyncHandlers.string(() -> customName, this::setCustomName)
                                                .allowC2S()))));

        mainWidget.child(Flow.col()
                .coverChildren()
                .childPadding(4)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .center()
                .child(new TextWidget<>(Text.dynamic(() -> name(shownName.getStringValue()))))
                .child(new TextWidget<>(Text.dynamic(() -> !connected.getBoolValue() ?
                        Component.translatable(KEY + "no_matrix").withStyle(ChatFormatting.RED) :
                        online.getBoolValue() ? Component.translatable("gtceu.gui.me_network.online") :
                                Component.translatable("gtceu.gui.me_network.offline"))))
                .child(Flow.row()
                        .coverChildren()
                        .childPadding(2)
                        .collapseDisabledChildren()
                        .child(new ButtonWidget<>()
                                .size(18)
                                .overlay(Text.str("✎").asIcon().size(16))
                                .tooltipBuilder(tooltip -> tooltip.addLine(Text.lang(KEY + "rename")))
                                .onMousePressed((context, button) -> {
                                    renaming.togglePanel();
                                    return true;
                                }))
                        .child(GTMuiWidgets.createToggleButton(this::isAutoUpload, this::setAutoUpload,
                                IO.OUT.uiTexture, AssemblingMatrixMachine.KEY + "auto_upload")
                                .setEnabledIf(button -> connected.getBoolValue()))));
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
}
