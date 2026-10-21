+++
title = "AEStackLikeBridge: opt-in AE support for a stack-like capability (capability <-> AEKey); chemicals via AppMek"
keys = [ "aestacklikebridges", "applied mekanistics", "mekanismkey", "stack-like ae" ]
updated = "2026-10-21 08:14:24"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/AEStackLikeBridge.java"
snippet = "public interface AEStackLikeBridge<S, I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/AppMekChemicalBridge.java"
snippet = "public final class AppMekChemicalBridge implements AEStackLikeBridge<ChemicalStack, ChemicalStackIngredient> {"
+++
Must be registered in the GTMQoL constructor before any machine exists: ME parts and the pattern buffer attach one aggregate handler per bridge in their constructors (the buffer's per-slot handlers are made lazily). Once registered, the universal parts (stacklike.ae-universal-parts), pattern buffer (stacklike.pattern-buffer), PatternBufferReturn and recipedb PatternBufferIngredients take the type with no further changes.