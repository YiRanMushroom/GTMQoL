+++
title = "AEConfigSyncHandlerMixin: ME stocking / input bus config UI didn't update while open; resend all on change"
keys = [ "detectandsendchanges", "dual input" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/AEConfigSyncHandlerMixin.java"
snippet = "public abstract class AEConfigSyncHandlerMixin {"
+++
1.21 only. Also affects our ME dual input UI. The encode-twice root cause is a guess.