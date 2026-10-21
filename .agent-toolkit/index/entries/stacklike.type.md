+++
title = "GenericStackLikeType: describes a fluid-like stack type (type + long amount) for the stack-like capability"
keys = [ "1.21", "chemicals", "getstacks", "lookupkey", "readingredient", "recipe capability", "stack-like core", "withamount", "writeingredient" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeType.java"
snippet = "public interface GenericStackLikeType<S, I> {"
+++
One generic capability/handler/display path covers every type; a type only describes its stacks. User's rules: whatever enters a recipe has a real RecipeCapability (no cap for AE keys as such, so other mods' hatches can implement it); AE support is opt-in per cap via a bridge (stacklike.ae-bridge). Network IO is writeIngredient/readIngredient on FriendlyByteBuf (1.21 uses a StreamCodec). Only type: Mekanism chemicals (stacklike.chemical). How to add one: stacklike.adding-a-type.