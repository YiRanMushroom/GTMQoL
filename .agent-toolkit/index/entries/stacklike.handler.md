+++
title = "GenericStackLikeNotifiableHandler: recipe handler base for stack-like caps; subclasses only provide storage"
keys = [ "consumed inputs", "getrecipemachine", "handlerecipeinner", "notifiablerecipehandlertrait", "stack-like core", "unattached trait" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java"
snippet = "public abstract class GenericStackLikeNotifiableHandler<S, I> extends NotifiableRecipeHandlerTrait<I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java"
snippet = "protected @Nullable MetaMachine getRecipeMachine() {"
+++
Subclasses: MEUniversalInputPartMachine.BridgedView, MEUniversalOutputPartMachine.BridgedOutput (stacklike.ae-universal-parts), PatternBufferStackLike.SlotHandler/Aggregate (stacklike.pattern-buffer), and PatternBufferReturn's bridged return. Per-slot handlers aren't attached traits, so they override getRecipeMachine (getMachine throws for them).