+++
title = "GameDataMixin: duplicate gtceu:recipe_type RegisterEvent only for gtceu/kubejs; ATTRIBUTE first again"
keys = [ "atm10", "core lib", "cyclopscore", "duplicate registration", "getregistrationorder", "modernfix", "registration order", "swim_speed" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/gtceufix/GameDataMixin.java"
snippet = "public class GameDataMixin {"
+++
1.21 only. GTCEu's own GameDataMixin maps gtceu:recipe_type onto the vanilla recipe type registry, so it is posted twice. Without this fix ATM10 failed to load. Uses WrapOperation (not Redirect) because ModernFix @Redirects the same call.