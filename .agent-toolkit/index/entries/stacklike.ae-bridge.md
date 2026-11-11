+++
title = "AEStackLikeBridge: opt-in AE support for a stack-like capability (capability <-> AEKey); chemicals via AppMek"
keys = [ "aestacklikebridges", "applied mekanistics", "mekanismkey", "stack-like ae" ]
updated = "2026-11-11 08:35:17"
checked = "2026-11-11"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/ae2/AEStackLikeBridge.java"
snippet = "public interface AEStackLikeBridge<S, I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/ae2/AppMekChemicalBridge.java"
snippet = "public final class AppMekChemicalBridge implements AEStackLikeBridge<ChemicalStack, ChemicalStackIngredient> {"
+++
Must be registered in the GTMQoL constructor before any machine exists: ME parts and the pattern buffer attach one aggregate handler per bridge in their constructors (the buffer's per-slot handlers are made lazily). Once registered, the universal parts (stacklike.ae-universal-parts), pattern buffer (stacklike.pattern-buffer), PatternBufferReturn and recipedb PatternBufferIngredients take the type with no further changes.

Mekanism itself (10.7, checked 2026-11-11) has no AE2 code, only compat recipes (data/mekanism/recipe/compat/ae2: certus, fluix, sky stone). Everything about chemicals in AE comes from Applied Mekanistics: the MekanismKey type, chemical cells and portable cells, storage/import/export buses on chemical handlers, the chemical capability on interfaces and pattern providers, the Chemical P2P tunnel, filling tanks from the terminal, EMI/JEI pattern encoding, a storage bus on a QIO Dashboard, and keeping radioactive chemicals out of cells, void overflow and the condenser. GTMQoL has no non-ME chemical hatch, so without AppMek nothing moves chemicals in or out of GT machines (the gregified Mekanism chemical recipes still show up in EMI but can't run).