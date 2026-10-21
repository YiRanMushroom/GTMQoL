+++
title = "Machine tooltip lang keys end in .desc, never <ns>.machine.<id>.tooltip (gtceu adds that key itself)"
keys = [ "appendhovertext", "duplicate tooltip", "metamachineblock" ]
updated = "2026-10-19 20:12:38"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/integration/ae2/AEDualParts.java"
snippet = "private static final String UNIVERSAL_INPUT_TOOLTIP = \"gtmqol.machine.me_universal_input.desc\";"
+++
gtceu's MetaMachineBlock.appendHoverText adds <ns>.machine.<id>.tooltip by itself, so a tooltip we also add under that key shows twice.