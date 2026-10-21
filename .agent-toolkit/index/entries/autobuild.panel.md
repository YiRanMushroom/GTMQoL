+++
title = "AutoBuildPanel: client-only right-click settings window, sliders and block toggles, no 3D preview"
keys = [ "auto-build", "ipanelhandler.simple" ]
updated = "2026-10-19 20:12:36"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildPanel.java"
snippet = "public static ModularPanel<?> build(MultiblockControllerMachine controller) {"
+++
No synced values (opened with IPanelHandler.simple only on the client). No 3D preview because GTCEu's preview was buggy. UI text must stay free of implementation details (tick counts, "kept until restart"), per the user.