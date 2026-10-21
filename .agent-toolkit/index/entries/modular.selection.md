+++
title = "Which machines get a modular multiblock: MachineBuilderMixin hands every register() over, first >=LV per type set wins"
keys = [ "hp_", "kubejs", "recipe types", "registertieredmachines", "steam", "tier" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "ModularMachines.register(cir.getReturnValue());"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "if (!RECIPE_TYPES.add(Set.copyOf(Arrays.asList(recipeTypes)))) return;"
+++
User's rule (2026-10-08): per set of recipe types, the first machine with tier >= LV wins (steam ones share types but are tier 0). The VN-prefix check keeps out hp_ steam machines, registered with tier(1) = LV. Hooking register() (full descriptor, not the BuilderBase bridge) replaced the old RETURN of registerTieredMachines + KJSTieredMachineBuilder mixin, which missed self-looping addons. Type sets are recorded even with the config off.