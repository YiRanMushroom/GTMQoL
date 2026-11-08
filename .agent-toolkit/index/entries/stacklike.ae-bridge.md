+++
title = "AEStackLikeBridge: opt-in AE support for a stack-like capability (capability <-> AEKey); chemicals via AppMek"
keys = [ "aestacklikebridges", "applied mekanistics", "mekanismkey", "stack-like ae" ]
updated = "2026-11-07 10:18:40"
checked = "2026-11-07"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/GTMQoL.java"
snippet = "AEStackLikeBridges.register(AppMekChemicalBridge.INSTANCE);"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/ae2/AEStackLikeBridge.java"
snippet = "public interface AEStackLikeBridge<S, I> {"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/stacklike/ae2/AppMekChemicalBridge.java"
snippet = "implements AEStackLikeBridge<ChemicalStack<?>, ChemicalStackIngredient<?, ?>> {"
+++
Registered (AEStackLikeBridges.register) at the top of the AE2 block in GTMQoL.onRegisterMachines (Mek + appmek loaded), before any machine exists: ME parts and the pattern buffer attach one handler per bridge in their constructors. Then the universal parts, pattern buffer, PatternBufferReturn and recipedb PatternBufferIngredients take the type unchanged. Applied Mekanistics 1.4.3 (Modrinth version id in gradle.properties appmek_version): MekanismKey wraps ChemicalStack<?>, so one bridge covers all four kinds.