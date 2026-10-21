+++
title = "1.20.1 after the Loom -> ModDevGradle legacyForge migration: what to check on the first dev client run"
keys = [ "in-game check", "runclient" ]
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[items]]
name = "jade-this0"
value = "Loom needed nameSyntheticMembers = true or Jade's StringRenderOutputMixin failed (@Shadow this$0 not located in Font$StringRenderOutput). MDG/parchment has no such switch: check Jade loads."

[[items]]
name = "evalex-runtime"
value = "EvalEx (jar-in-jar of ModularUI, used by GTCEu's PipeBlock) is now plain runtimeOnly instead of Loom's forgeRuntimeLibrary: check no NoClassDefFoundError com/ezylang/evalex and gtceu:*_wire entries register."
path = "build.gradle"
snippet = "runtimeOnly \"com.ezylang:EvalEx:${project.evalex_version}\""

[[items]]
name = "productivelib"
value = "ProductiveLib (jar-in-jar in Productive Bees, published nowhere) isn't declared; unknown whether the dev runtime loads it from PB's jar or PB fails to start."

[[items]]
name = "gtceu-jarjar"
value = "We still use gtceu :slim plus explicitly declared inner jars. Open: whether the full gtceu jar's jarjar now works under MDG (Loom stripped metadata.json); only worth trying if the explicit list becomes a burden."

[[items]]
name = "client-run-config"
value = "Client run config / args from build.gradle legacyForge { runs } untested (the data run works)."
+++
Migration `3b63374` (build.gradle follows GTCEu's 1.20.1 branch). compileJava and runData pass; the client has not been run under MDG. Remove each item once it is confirmed in game.