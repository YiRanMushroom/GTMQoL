+++
title = "How to add a new stack-like type (capability, handler, AE bridge, JEI/EMI, recipe types)"
keys = [ "external hatches", "stack-like core" ]
updated = "2026-10-21 01:13:03"
+++
1. Implement `GenericStackLikeType<S, I>` (model: `ChemicalStackLike`, stacklike.chemical), incl. codecs and `writeIngredient`/`readIngredient`. Keep it in a class touched only when the mod is loaded.
2. Give that class a static `init()` doing `CAP = GenericStackLikeRecipeCapability.register(TYPE, color, sortIndex, name)`, called from the `GTMQoL` constructor behind `ModList.get().isLoaded(...)` (like `ChemicalStackLike.init()`). Registration into gtceu's registry is automatic (`registerAll` on the RecipeCapability RegisterEvent, stacklike.recipe-capability). Sort index between gtceu's (chemicals took 4, between cwu 3 and block_state 5).
3. A part/machine holding it: subclass `GenericStackLikeNotifiableHandler` (stacklike.handler).
4. AE (optional): an `AEStackLikeBridge`, registered in `GTMQoL.onRegisterMachines` before the AE machines (stacklike.ae-bridge). Universal ME parts and the pattern buffer then take it unchanged.
5. Recipe viewers: JEI needs the stack class registered as a JEI ingredient type (the other mod usually does it); lookup/encoding then work via `StackLikeJei` (stacklike.jei). EMI needs an `EmiStackConverter.Converter<S>` registered in `GTMQoLClient.init` (stacklike.emi-converter). Layout/content defaults are automatic (stacklike.emi-display).
6. Recipe types using it: `setMaxSize(IO, CAP, n)` (gregification: `ForeignMachineType.withRecipeType`).

Open todo: external chemical hatches.

Reference sources: gtceu sources in `.gtceu-src/` (`FluidRecipeCapability`, `NotifiableFluidTank`, `api/recipe/gui/*`, `integration/ae2/machine/MEPatternBufferPartMachine`, `integration/recipeviewer/jei|emi`); ModularUI sources in `.mui-src/`; Mekanism 10.4 sources jar in Gradle's modules-2 cache (`mekanism/Mekanism/1.20.1-<mekanism_version>/`).