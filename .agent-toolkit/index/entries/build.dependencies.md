+++
title = "Dependencies: GTCEu's jar-in-jars declared by hand, optional mods only touched when loaded, where sources are"
keys = [ "jarjar" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "build.gradle"
snippet = "accessCompileOnly(\"com.gregtechceu.gtceu:gtceu-${project.minecraft_version}:${project.gtceu_version}\") {"
+++
GTCEu's pom lists none of its jar-in-jars, so each is declared here. Truth is META-INF/jarjar/metadata.json in the full GTCEu jar, recursively (MUI embeds EvalEx); recheck on bumps (build.gtceu-upgrade). v8 has no LDLib; the Gradle cache may hold 7.x and 8.x jars. Optional mods: never in neoforge.mods.toml, touched only when ModList / GTCEu.Mods.isXLoaded / LoadingModList has them; Modrinth ones transitive=false, version IDs not numbers. Sources: .gtceu-src-1.21; FTB Teams on GitHub.