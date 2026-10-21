+++
title = "Block capability in recipe viewers: catalyst item slot via UI layout mixin, EMI input with chance 0"
keys = [ "ae encoding", "block capability", "setmaxsize" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/blockprovider/BlockProviderRecipeViewer.java"
snippet = "public static void addDefaults(GTRecipeTypeUILayout.Builder builder) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeBlockProviderMixin.java"
snippet = "private List<EmiIngredient> gtmqol$addBlockInputs(List<EmiIngredient> inputs) {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTRecipeTypeUILayoutBuilderMixin.java"
snippet = "BlockProviderRecipeViewer.addDefaults((GTRecipeTypeUILayout.Builder) (Object) this);"
+++
Recipe types need setMaxSize(IO.IN, CAP, n) to get slots. Chance 0 in EMI so AE pattern encoding skips the block. 1.20.1 also has GTRecipeJEICategoryBlockProviderMixin (invisible catalyst).