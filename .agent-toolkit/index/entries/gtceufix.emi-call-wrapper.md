+++
title = "EmiCallWrapperMixin: machine UI recipe type button threw NoSuchMethodError (EmiApi.setPages invoker never applied)"
keys = [ "displayrecipecategory", "emiapiaccessor" ]
updated = "2026-10-21 08:10:50"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/EmiCallWrapperMixin.java"
snippet = "public class EmiCallWrapperMixin {"
+++
1.21 only. Opens the machine's category through EMI's public API (EmiApi.displayRecipeCategory); unlike gtceu's version, the type's other categories aren't shown as tabs. EMI is compileOnly (plus localRuntime in dev); this mixin is one of several EMI users (ghost/stack-like EMI converters, GTEmiRecipe* mixins, pattern encoding).