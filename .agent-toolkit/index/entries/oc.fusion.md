+++
title = "overclocking.buffFusionReactor: perfect subtick fusion OC on full hatch voltage, substation/laser hatches allowed"
keys = [ "fusion_oc", "getoverclockvoltage" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/FusionReactorMachineMixin.java"
snippet = "private static OverclockingLogic gtmqol$fusionOC(double durationFactor, double voltageFactor, boolean subTick,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/FusionReactorMachineMixin.java"
snippet = "private static ModifierFunction gtmqol$fusionOCVoltage(OverclockingLogic logic, MetaMachine machine,"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/core/mixins/GTMultiMachinesMixin.java"
snippet = "private static Collection<Block> gtmqol$fusionEnergyHatches(PartAbility ability, int from, int to,"
+++
gtmqol.yaml (toma) option, default true, restart (FUSION_OC is set in <clinit>). Upstream uses tier-capped getMaxVoltage(); we pass getOverclockVoltage() with parallels on. The hatch mixin selects lambdas by regex `/^lambda\$/` (only the fusion lambda calls getBlockRange); if it misses, use the exact name from javap -p.