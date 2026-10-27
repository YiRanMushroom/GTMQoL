+++
title = "AutoBuildPanel: client-only right-click settings, GTCEu's multiblock preview turned like the controller"
keys = [ "3d preview", "auto-build", "ipanelhandler.simple", "multiblockpreviewwidget", "multiblockschemainfo" ]
updated = "2026-10-27 15:31:27"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildPanel.java"
snippet = "public static ModularPanel<?> build(MultiblockControllerMachine controller) {"
+++
No synced values (opened with IPanelHandler.simple only on the client). Since GTCEu #5337 (2026-10-27, user's call) the window is GTCEu's MultiblockPreviewWidget without its terminal buttons, editing a MultiblockSchemaInfo per multiblock type; positions picked in it only count for the controller they were picked on. Before, sliders and toggles, because the old preview was buggy. 1.21 keeps sliders until upstream ports #5337. UI text without implementation details, per the user.