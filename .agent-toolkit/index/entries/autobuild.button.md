+++
title = "Survival auto-build button on every multiblock controller (misc.autoBuild): click, shift-click job, right-click settings"
keys = [ "buildrequest", "synchandler", "terminal button" ]
updated = "2026-10-19 20:12:36"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/autobuild/AutoBuildButton.java"
snippet = "private static final class BuildRequest extends SyncHandler<BuildRequest> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineUIPanelBuilderMixin.java"
snippet = "AutoBuildButton.attach(controller, cir.getReturnValue(), syncManager);"
+++
Same design on 1.21.1. The click is handled on the client and sent C2S (FriendlyByteBuf here) with the client's BuildSettings (autobuild.settings); the server runs AutoBuilder.onPressed. The config check is in the mixin; AutoBuilder.init always runs. Next planned: substructures, then block choices for expandable patterns.