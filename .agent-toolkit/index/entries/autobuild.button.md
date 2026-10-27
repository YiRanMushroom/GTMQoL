+++
title = "Survival auto-build button on every multiblock controller (misc.autoBuild): click, shift-click job, right-click settings"
keys = [ "buildrequest", "synchandler", "terminal button" ]
updated = "2026-10-27 15:32:00"
checked = "2026-10-27"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineUIPanelBuilderMixin.java"
snippet = "AutoBuildButton.attach(controller, cir.getReturnValue(), syncManager);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildButton.java"
snippet = "private static final class BuildRequest extends SyncHandler<BuildRequest> {"
+++
Both branches. The click is handled on the client and sent C2S with the client's BuildSettings (autobuild.settings); the server runs AutoBuilder.onPressed. 1.20.1 differs in FriendlyByteBuf and Forge 47 events, and uses GTCEu's preview as the settings window (#5337). Decision (user, 2026-10-27): keep our button even though GTCEu's terminal now auto-builds; reuse GTCEu's preview UI instead of our own window where it exists.