+++
title = "1.20.1 build: ModDevGradle legacyForge, Gradle on JDK 21 with a JDK 17 toolchain, mod* configurations, JEI/EAP pins"
updated = "2026-10-19 20:12:37"
checked = "2026-10-21"

[[links]]
path = "gradle.properties"
snippet = "org.gradle.java.installations.fromEnv=JDK17"

[[links]]
path = "build.gradle"
snippet = "id 'net.neoforged.moddev.legacyforge' version '2.0.141'"
+++
legacyForge like GTCEu's 1.20.1 branch (moved from Loom in 3b63374): mod* remapping configurations, modLocalRuntime via createRemappingConfiguration. Gradle JVM: JAVA_HOME=~/.jdks/openjdk-21.0.1; the Java 17 toolchain comes from env JDK17 (C:\Program Files\Java\jdk-17). Mixin configs and refmap in build.gradle mixin {}; MixinExtras is jarJar'd. JEI and EAP are pinned by GTCEu's JEI mixins (see gradle.properties). Open first-run checks: build.first-run-checks.