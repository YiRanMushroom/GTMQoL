+++
title = "EMI real slots and lookup for stack-like stacks: EmiStackConverter per stack class, GTEmiRecipe inputs/outputs mixin"
keys = [ "emirecipeviewerslot", "imekanismemihelper", "pattern encoding", "r/u", "stack-like viewers", "stacklikeemi" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalEmiConverter.java"
snippet = "EmiStackConverter.register(ChemicalStack.class, new ChemicalEmiConverter());"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/client/GTMQoLClient.java"
snippet = "if (ModList.get().isLoaded(\"emi\") && ModList.get().isLoaded(\"mekanism\")) ChemicalEmiConverter.register();"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTEmiRecipeStackLikeMixin.java"
snippet = "public abstract class GTEmiRecipeStackLikeMixin {"
+++
MUI's RecipeViewerSlotWidget.create(cls) gives an EmiRecipeViewerSlot wrapping a real EMI SlotWidget, so hover, tooltip, R/U and favourites come from EMI; it converts via EmiStackConverter. A cap without a converter shows empty slots and is skipped by EMI lookup and AE2 pattern encoding (EmiStackHelper.ofInputs -> getInputs; AppMek has its own EMI chemical converter). Chemical stacks are built with IMekanismAccess.INSTANCE.emiHelper().