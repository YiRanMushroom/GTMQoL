+++
title = "Which machines get a modular multiblock: MachineBuilderMixin queues all, first >=LV per recipe-type set wins"
keys = [ "hp_", "kubejs", "machinebuilderaccessor", "recipe types", "registertieredmachines", "steam", "tier" ]
updated = "2026-10-19 20:08:34"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/MachineBuilderMixin.java"
snippet = "ModularMachines.queue(self.getOwner().getModid(), self.getName(), self.properties().tier(),"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/modular/ModularMachines.java"
snippet = "public static void queue(String namespace, String name, int tier, MachineEntry<?> simple,"
+++
User's rule (2026-10-08): per set of recipe types, the first machine with tier >= LV wins (steam ones share types but are tier 0). The VN-prefix check exists because gtceu and addons register hp_ steam machines with tier(1) = LV. Hooking register() replaced the old 1.20.1 approach (RETURN of registerTieredMachines + a KJSTieredMachineBuilder mixin) that missed self-looping addons. Late (KubeJS) machines get none and log a warning.