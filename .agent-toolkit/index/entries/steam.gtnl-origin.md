+++
title = "Steam content design: modelled on GTNL (LGPL-3.0), user's design choices, license"
keys = [ "1.21", "gtnh", "gtnl-ref", "known issues", "large steam multis", "steam ui" ]
updated = "2026-11-07 10:20:10"
checked = "2026-11-07"

[[items]]
name = "license"
value = "Project license LGPL-3.0 like GTNL"
path = "gradle.properties"
snippet = "mod_license=LGPL-3.0"

[[items]]
name = "license-in-jar"
value = "LICENSE packed into the jar"
path = "build.gradle"
snippet = "from('LICENSE')"

[[items]]
name = "readme-credit"
value = "README credits GTNL for the control circuit textures"
path = "README.md"
snippet = "addon. The control circuit textures are taken from GTNL and are licensed under LGPL-3.0."

[[items]]
name = "shapes"
value = "Large steam multi structures converted from GTNL (steam.shapes)"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamShapes.java"
snippet = "converted from GTNL's large steam multiblocks"

[[items]]
name = "circuit-assembler-mv"
value = "Large steam circuit assembler is MV so it makes the ULV-HV control circuits"
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamMachines.java"
snippet = "// MV makes the ULV-HV control circuits (see ControlCircuits)."
+++
- From GTNL (GTNH addon, LGPL-3.0), credited in README: the large steam multis' structures (converted from its .mbs files, steam.shapes) and the control circuit textures; the machines and their logic are written for GTCEu here. A reference clone is at `E:\code\Minecraft\gtnl-ref` on one of the user's computers (outside the repo).
- The project is LGPL-3.0 like GTNL/GTNH (`mod_license=LGPL-3.0` in gradle.properties): `LICENSE` (text copied from GTNL) is also packed into the jar (`from('LICENSE')` in build.gradle).
- User's choices: steam multis are bronze only (GTCEu's steam multis have no steel tier); no LV machines or circuits in the large steam multis' recipes (only the optional parallel hatch takes LV circuits, the LP steam circuit assembler takes one LV circuit); the magical assembler multi is LV, the circuit assembler MV.