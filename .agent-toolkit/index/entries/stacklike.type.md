+++
title = "GenericStackLikeType: describes a fluid-like stack type (type + long amount) for the stack-like capability"
keys = [ "chemicals", "getstacks", "lookupkey", "recipe capability", "stack-like core", "withamount" ]
updated = "2026-10-19 20:08:35"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeType.java"
snippet = "public interface GenericStackLikeType<S, I> {"
+++
One generic capability/handler/display path covers every type; a new type only describes its stacks. User's rules: whatever enters a recipe has a real RecipeCapability (no capability for AE keys as such, so other mods' hatches can implement it); AE support is opt-in per capability via a bridge (stacklike.ae-bridge); dedicated caps (items, fluids) have priority over the universal parts. Only type so far: Mekanism chemicals (stacklike.chemical). How to add one: stacklike.adding-a-type.