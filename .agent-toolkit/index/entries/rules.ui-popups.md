+++
title = "Popups: PopupWidgets.popup (movable, main UI stays usable), opened by IPanelHandler.togglePanel; counterRow"
keys = [ "counterrow", "popup", "popuppanel", "togglepanel", "ui" ]
updated = "2026-10-19 20:27:45"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/ui/PopupWidgets.java"
snippet = "public final class PopupWidgets {"
+++
Every popup of ours goes through PopupWidgets.popup instead of PopupPanel.createPopupPanel, so they all behave the same (void miner, fishing pond, cyclic stored list, ghost marks, smart doubling). Its button calls MUI's IPanelHandler.togglePanel() (second click closes), not openPanel(). Number settings in a popup: counterRow.