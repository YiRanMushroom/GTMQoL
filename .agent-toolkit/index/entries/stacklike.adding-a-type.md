+++
title = "How to add a new stack-like type (capability, handler, AE bridge, JEI/EMI, recipe types)"
keys = [ "external hatches", "stack-like core" ]
updated = "2026-11-07 09:56:13"
checked = "2026-11-07"

[[items]]
name = "type-interface"
value = "Step 1: the type to implement"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeType.java"
snippet = "void writeIngredient(FriendlyByteBuf buf, I ingredient);"

[[items]]
name = "register-cap"
value = "Step 2: CAP creation and the model init()"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java"
snippet = "CAP = GenericStackLikeRecipeCapability.register(TYPE, 0xFF9C5ED6, 4, \"Chemical\");"

[[items]]
name = "init-call"
value = "Step 2: init() behind ModList in the GTMQoL constructor"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "if (ModList.get().isLoaded(\"mekanism\")) ChemicalStackLike.init();"

[[items]]
name = "ae-bridge"
value = "Step 4: bridge registration in onRegisterMachines"
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);"

[[items]]
name = "emi-converter"
value = "Step 5: EMI converter registered from GTMQoLClient.init"
path = "src/main/java/com/yiran/minecraft/gtmqol/client/GTMQoLClient.java"
snippet = "ChemicalEmiConverter.register();"

[[items]]
name = "handler"
value = "Step 3: base class for parts holding the type"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java"
snippet = "public abstract class GenericStackLikeNotifiableHandler<S, I> extends NotifiableRecipeHandlerTrait<I> {"
+++
1. Implement `GenericStackLikeType<S, I>` (model: `ChemicalStackLike`, stacklike.chemical), incl. codecs and `writeIngredient`/`readIngredient`. Keep it in a class touched only when the mod is loaded.
2. Give that class a static `init()` doing `CAP = GenericStackLikeRecipeCapability.register(TYPE, color, sortIndex, name)`, called from the `GTMQoL` constructor behind `ModList.get().isLoaded(...)` (like `ChemicalStackLike.init()`). Registration into gtceu's registry is automatic (`registerAll` on the RecipeCapability RegisterEvent, stacklike.recipe-capability). Sort index between gtceu's (chemicals took 4, between cwu 3 and block_state 5).
3. A part/machine holding it: subclass `GenericStackLikeNotifiableHandler` (stacklike.handler).
4. AE (optional): an `AEStackLikeBridge`, registered in `GTMQoL.onRegisterMachines` before the AE machines (stacklike.ae-bridge). Universal ME parts and the pattern buffer then take it unchanged.
5. Recipe viewers: JEI needs the stack class registered as a JEI ingredient type (the other mod usually does it); lookup/encoding then work via `StackLikeJei` (stacklike.jei). EMI needs an `EmiStackConverter.Converter<S>` registered in `GTMQoLClient.init` (stacklike.emi-converter). Layout/content defaults are automatic (stacklike.emi-display).
6. Recipe types using it: `setMaxSize(IO, CAP, n)` (gregification: `ForeignMachineType.withRecipeType`).

Open todo: external chemical hatches.

Reference sources: gtceu sources in `.gtceu-src/` (`FluidRecipeCapability`, `NotifiableFluidTank`, `api/recipe/gui/*`, `integration/ae2/machine/MEPatternBufferPartMachine`, `integration/recipeviewer/jei|emi`); ModularUI sources in `.mui-src/`; Mekanism 10.4 sources jar in Gradle's modules-2 cache (`mekanism/Mekanism/1.20.1-<mekanism_version>/`).