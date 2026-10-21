+++
title = "Dependencies: GTCEu :slim plus its jar-in-jars by hand, optional mods only touched when loaded, sources"
keys = [ "jarjar" ]
updated = "2026-10-19 20:12:37"

[[links]]
path = "build.gradle"
snippet = "modImplementation \"com.gregtechceu.gtceu:gtceu-${project.minecraft_version}:${project.gtceu_version}:slim\""
+++
GTCEu's pom lists none of its jar-in-jars, so each is declared here. Truth is META-INF/jarjar/metadata.json in the full GTCEu jar, recursively (MUI embeds EvalEx); recheck on bumps (build.gtceu-upgrade). v8 has no LDLib; the Gradle cache may hold 7.x and 8.x jars. Optional mods: never in mods.toml, touched only when ModList / GTCEu.Mods.isXLoaded / LoadingModList has them; Modrinth ones transitive=false. Sources: .gtceu-src (unpacked sources jar); FTB Teams on GitHub 1.20.1/dev.