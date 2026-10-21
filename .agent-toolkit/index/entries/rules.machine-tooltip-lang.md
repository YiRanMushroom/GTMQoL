+++
title = "Machine tooltips: gtceu shows <ns>.machine.<id>.tooltip itself; never pass that key to .tooltips(), use .desc there"
keys = [ "appendhovertext", "duplicate tooltip", "metamachineblock" ]
updated = "2026-10-21 08:15:00"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "private static final String UNIVERSAL_INPUT_TOOLTIP = \"gtmqol.machine.me_universal_input.desc\";"
+++
gtceu's MetaMachineBlock.appendHoverText shows <ns>.machine.<id>.tooltip by itself. Either just register lang under that key (gtceu shows it; ProbableDevices, Create sequenced assembly) or, for tooltips passed via .tooltips(...) or with arguments, use a .desc key (AEDualParts *.desc, steam_parallel_hatch.desc). Never pass the .tooltip key to .tooltips(), or it shows twice.