+++
title = "ControlCircuits: cheap ULV-EV control circuits (GTNL MetaItem 21-25) made in the circuit assembler"
keys = [ "circuits.controlcircuits", "steam tweaks", "very_simple_control_circuit" ]
updated = "2026-10-19 20:12:38"
checked = "2026-10-21"

[[links]]
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/ControlCircuits.java"
snippet = "public final class ControlCircuits {"
+++
Recipes loosely follow GTNL's CircuitAssemblerRecipes with GTCEu materials (cast iron -> wrought iron); each tier consumes the previous one, ULV-HV use ghost circuit 14. Tagged with GTCEu's tier circuit tags (CustomTags.CIRCUITS_ARRAY). Textures from GTNL. Names overlap Mekanism's basic/advanced/elite control circuits (display only, different ids; see the CircuitTags forge: tag link).