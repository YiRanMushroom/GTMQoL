+++
title = "Steam content design: modelled on GTNL (LGPL-3.0), user's design choices, license"
keys = [ "gtnh", "gtnl-ref", "large steam multis" ]
updated = "2026-10-19 20:56:54"
+++
- The advanced steam multis, control circuits and shapes are modelled on GTNL's steam multis (GTNH addon, LGPL-3.0), not a copy; credited in README. A reference clone is at `E:\code\Minecraft\gtnl-ref` on one of the user's computers (outside the repo).
- The whole project is LGPL-3.0 like GTNL/GTNH: `LICENSE` (text copied from GTNL) is also packed into the jar (`from('LICENSE')` in build.gradle); in-game authors (mods.toml) "Yiran, Frosty", README and LICENSE say Yiran only (Frosty is the same person's alias).
- User's choices: steam multis are bronze only (GTCEu's steam multis have no steel tier); no LV machines or circuits in the large steam multis' recipes (only the optional parallel hatch takes LV circuits, the LP steam circuit assembler takes one LV circuit); the magical assembler multi is LV, the circuit assembler MV.