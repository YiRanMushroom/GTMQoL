---
name: stacklike-type
description: "Stack-like types: add one (capability, handler, AE bridge, EMI converter); use when supporting a new stack-like resource"
---

<!-- agent-toolkit: generated from index entry skill.stacklike-type: change it with index_update("skill.stacklike-type"), not here -->

1. Implement `GenericStackLikeType<S, I>` (see `ChemicalStackLike`, stacklike.chemical). Keep it in a class touched only when the mod is loaded.
2. Give that class a static `init()` doing `CAP = GenericStackLikeRecipeCapability.register(TYPE, color, sortIndex, name)` (stacklike.recipe-capability), called from the `GTMQoL` constructor behind `ModList.get().isLoaded(...)` (like `ChemicalStackLike.init()`). Pick a sort index between gtceu's (chemicals took 4, between cwu 3 and block_state 5).
3. A part or machine holding it: subclass `GenericStackLikeNotifiableHandler` (stacklike.handler).
4. AE (optional): an `AEStackLikeBridge` for the mod that puts the type into ME networks, registered in the constructor before the machines (stacklike.ae-bridge). The universal ME parts and the pattern buffer then take it with no further changes.
5. EMI (optional): an `EmiStackConverter.Converter<S>`, registered in `GTMQoLClient.init` behind EMI + the mod (stacklike.emi-converter). Without it the slots stay empty, and EMI lookup / AE pattern encoding skip the type. Layout/content defaults are automatic (stacklike.emi-display).
6. Recipe types using it: `setMaxSize(IO, CAP, n)` (gregification: `ForeignMachineType.withRecipeType`).

Reference sources: gtceu 1.21 sources in `.gtceu-src-1.21/` (`FluidRecipeCapability` and `NotifiableFluidTank` are what we mirror; `api/recipe/gui/*` for the viewer; `integration/ae2/machine/MEPatternBufferPartMachine`); ModularUI sources in `.mui-src-1.21/brachy/modularui/integration/` (`emi/EmiRecipeViewerSlot`, `emi/EmiStackConverter`, `recipeviewer/RecipeViewerSlotWidget`); Mekanism sources jar in Gradle's modules-2 cache (`mekanism/Mekanism/1.21.1-<mekanism_version>/`, has `api/integration/emi/IMekanismEmiHelper`); Applied Mekanistics jar (`maven.modrinth/applied-mekanistics`, `MekanismKey` + its EMI converter); AE2 sources jar (`AEKey`, `EmiStackHelper`).

- type: step 1: the type interface to implement
- cap-register: step 2: registers the recipe capability (color, sort index, name)
- chemical-init: the reference init() (sort index 4)
- handler: step 3: base class for parts/machines holding the type
- ae-bridge: step 4: AE bridge registration
- emi-converter: step 5: the EMI converter registered in GTMQoLClient.init

Code:
- src/main/java/com/yiran/minecraft/gtmqol/client/GTMQoLClient.java
- src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeNotifiableHandler.java
- src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeRecipeCapability.java
- src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/GenericStackLikeType.java
- src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/mekanism/ChemicalStackLike.java
- src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/AEStackLikeBridges.java
