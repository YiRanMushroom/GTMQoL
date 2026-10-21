+++
title = "GenericStackLikeNotifiableHandler: recipe handler base for stack-like caps; subclasses only provide storage"
keys = [ "getrecipemachine", "handlerecipeinner", "notifiablerecipehandlertrait", "stack-like core", "unattached trait" ]
updated = "2026-10-19 20:08:35"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java"
snippet = "public abstract class GenericStackLikeNotifiableHandler<S, I> extends NotifiableRecipeHandlerTrait<I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java"
snippet = "protected @Nullable MetaMachine getRecipeMachine() {"
+++
Subclasses: universal ME parts' views (stacklike.ae-universal-parts), pattern buffer slot/aggregate handlers (stacklike.pattern-buffer), PatternBufferReturn.BridgedReturn.