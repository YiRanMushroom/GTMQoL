+++
title = "1.21.1 build: ModDevGradle, Java 21, versions from GTCEu's 1.21 branch, EMI in dev, MixinExtras from NeoForge"
keys = [ "jdk" ]
updated = "2026-10-19 20:08:34"
checked = "2026-10-21"

[[links]]
path = "gradle.properties"
snippet = "org.gradle.java.installations.fromEnv=JDK21"

[[links]]
path = "build.gradle"
snippet = "id 'net.neoforged.moddev' version '2.0.141'"
+++
ModDevGradle like GTCEu's 1.21 branch and the official addon template: no remapping, no mod* configurations, dev-only mods on our own `localRuntime`. One JDK 21 for Gradle and client (JAVA_HOME=~/.jdks/openjdk-21.0.1; toolchain from env JDK21, else foojay). Versions follow GTCEu 1.21's gradle/*.versions.toml. Mixin configs are listed in neoforge.mods.toml [[mixins]]. MixinExtras comes with NeoForge (0.5.3 on 21.1.248): mixinextras.minVersion must not exceed it.