+++
title = "AEStackLikeBridge: opt-in AE support for a stack-like capability (capability <-> AEKey); chemicals via AppMek"
keys = [ "aestacklikebridges", "applied mekanistics", "mekanismkey", "stack-like ae" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/AEStackLikeBridge.java"
snippet = "public interface AEStackLikeBridge<S, I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/stacklike/AppMekChemicalBridge.java"
snippet = "implements AEStackLikeBridge<ChemicalStack<?>, ChemicalStackIngredient<?, ?>> {"
+++
Registered at the top of the AE2 block in GTMQoL.onRegisterMachines (Mek + appmek loaded), before any machine exists: ME parts and the pattern buffer attach one handler per bridge in their constructors. Then the universal parts, pattern buffer, PatternBufferReturn and recipedb PatternBufferIngredients take the type unchanged. Applied Mekanistics 1.4.3 (Modrinth id appmek_version): MekanismKey wraps ChemicalStack<?>, so one bridge covers all four kinds.