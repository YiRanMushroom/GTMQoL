+++
title = "Steam content design: modelled on GTNL (LGPL-3.0), user's design choices, license"
keys = [ "gtnh", "gtnl-ref", "large steam multis" ]
updated = "2026-11-07 10:19:45"
checked = "2026-11-07"

[[items]]
name = "readme-credit"
value = "README credits GTNL for the advanced steam multis and control circuits (textures LGPL-3.0)."
path = "README.md"
snippet = "[GT Not Leisure](https://github.com/Darknight2333/GT-Not-Leisure1) (GTNL), a GregTech: New Horizons"

[[items]]
name = "license-in-jar"
value = "LICENSE packed into the jar."
path = "build.gradle"
snippet = "from('LICENSE')"

[[items]]
name = "mod-author"
value = "In-game author list, put into neoforge.mods.toml as authors."
path = "gradle.properties"
snippet = "mod_author=Yiran, Frosty"

[[items]]
name = "shapes"
value = "Structures converted from GTNL's large steam multis (steam.shapes)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/steam/AdvancedSteamShapes.java"
snippet = "final class AdvancedSteamShapes {"

[[items]]
name = "control-circuits"
value = "Control circuits modelled on GTNL (steam.control-circuits)."
path = "src/main/java/com/yiran/minecraft/gtmqol/common/circuit/ControlCircuits.java"
snippet = "public final class ControlCircuits {"
+++
- From GTNL (GTNH addon, LGPL-3.0), credited in README: the large steam multis' structures (converted from its .mbs files, steam.shapes) and the control circuit textures; the machines and their logic are written for GTCEu here. A reference clone is at `E:\code\Minecraft\gtnl-ref` on one of the user's computers (outside the repo).
- The whole project is LGPL-3.0 like GTNL/GTNH: `LICENSE` (text copied from GTNL) is also packed into the jar (`from('LICENSE')` in build.gradle). Authors: README and LICENSE "Yiran"; in game `mod_author` in gradle.properties ("Yiran, Frosty", Frosty is the same person's alias), put into neoforge.mods.toml.
- User's choices: steam multis are bronze only (GTCEu's steam multis have no steel tier); no LV machines or circuits in the large steam multis' recipes (only the optional parallel hatch takes LV circuits, the LP steam circuit assembler takes one LV circuit); the magical assembler multi is LV, the circuit assembler MV.