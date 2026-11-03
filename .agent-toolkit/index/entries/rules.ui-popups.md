+++
title = "Popups: PopupWidgets.popup (movable, main UI stays usable), opened by IPanelHandler.togglePanel; counterRow"
keys = [ "popuppanel" ]
updated = "2026-11-03 03:35:02"
checked = "2026-11-03"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/PopupWidgets.java"
snippet = "public final class PopupWidgets {"
+++
Every popup of ours goes through PopupWidgets.popup instead of PopupPanel.createPopupPanel, so they all behave the same (void miner, fishing pond, cyclic stored list, ghost marks, smart doubling). Its button calls MUI's IPanelHandler.togglePanel() (second click closes), not openPanel(). Number settings in a popup: counterRow (click ±1, Shift ±shiftStep, Ctrl ×2/÷2).