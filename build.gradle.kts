import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.architectury.loom") version "1.13.467"
    id("maven-publish")
    kotlin("jvm")
}

base {
    archivesName.set(project.property("archives_base_name") as String)
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

loom {
    silentMojangMappingsLicense()

    runs {
        named("client") {
            programArgs("--width", "1280", "--height", "720")
            ideConfigGenerated(true)
            vmArgs("-Dmixin.debug.export=true")
            runDir("run")
        }
        named("server") {
            ideConfigGenerated(true)
            runDir("run")
        }
    }

    mods {
        create("gtmqol") {
            sourceSet(sourceSets.main.get())
        }
    }

    forge {
        // specify the mixin configs used in this mod
        // this will be added to the jar manifest as well!
        mixinConfig("gtmqol.mixins.json")

        // missing access transformers?
        // don't worry, you can still use them!
        // note that your AT *MUST* be located at
        // src/main/resources/META-INF/accesstransformer.cfg
        // to work as there is currently no config option to change this.
        // also, any names used in your access transformer will need to be
        // in SRG mapped ("func_" / "field_" with MCP class names) to work!
        // (both of these things may be subject to change in the future)
    }
}

repositories {
    mavenLocal()
    flatDir {
        dir("libs")
    }
    mavenCentral()
    maven {
        name = "FirstDarkDev"
        url = uri("https://maven.firstdarkdev.xyz/snapshots/")
    }
    maven {
        name = "GTCEu Maven"
        url = uri("https://maven.gtceu.com")
        content {
            includeGroup("com.gregtechceu.gtceu")
        }
    }
    maven {
        name = "Quilt"
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
    maven {
        url = uri("https://maven.saps.dev/releases/")
        content {
            includeGroup("dev.latvian.mods")
        }
    }
    maven { // Registrate
        url = uri("https://maven.tterrag.com/")
        content {
            // need to be specific here due to version overlaps
            includeGroup("com.jozufozu.flywheel")
            includeGroup("com.tterrag.registrate")
            includeGroup("com.simibubi.create")
        }
    }
    maven {
        // Patchouli, JEI
        name = "BlameJared"
        url = uri("https://maven.blamejared.com/")
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
    }
    maven {
        // Curse Forge File
        url = uri("https://cursemaven.com/")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        url = uri("https://maven.architectury.dev/")
    }
    maven {
        url = uri("https://maven.saps.dev/minecraft")
    }
    maven {
        name = "Configuration"
        url = uri("https://api.repsy.io/mvn/toma/public/")
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
        content {
            includeGroup("dev.emi")
        }
    }
    maven {
        name = "AE2"
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
        }
    }
    exclusiveContent { // FTB mods
        forRepository { maven { url = uri("https://maven.ftb.dev/releases") } }
        filter { includeGroup("dev.ftb.mods") }
    }
    maven { url = uri("https://jitpack.io") }
    maven {
        name = "GuideME Snapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content {
            includeModule("org.appliedenergistics", "guideme")
        }
    }
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven { url = uri("https://modmaven.dev/") }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    forge("net.minecraftforge:forge:${project.property("minecraft_version")}-${project.property("forge_version")}")

    // mojmap + parchment mappings
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-${project.property("minecraft_version")}:${project.property("parchment_mappings")}@zip")
    })

    modCompileOnly("mezz.jei:jei-${project.property("minecraft_version")}-forge-api:${project.property("jei_version")}") { isTransitive = false }
    modCompileOnly("mezz.jei:jei-${project.property("minecraft_version")}-common-api:${project.property("jei_version")}") { isTransitive = false }
    modCompileOnly("dev.emi:emi-forge:${project.property("emi_version")}:api") { isTransitive = false }

    modImplementation("com.gregtechceu.gtceu:gtceu-${project.property("minecraft_version")}:${project.property("gtceu_version")}") { isTransitive = false }
    modImplementation("com.lowdragmc.ldlib:ldlib-forge-${project.property("minecraft_version")}:${project.property("ldlib_version")}") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${project.property("registrate_version")}")

    modRuntimeOnly("dev.toma.configuration:configuration-forge-${project.property("minecraft_version")}:${project.property("configuration_version")}")
    modRuntimeOnly("mezz.jei:jei-${project.property("minecraft_version")}-forge:${project.property("jei_version")}") { isTransitive = false }

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${project.property("mixinextras_version")}")!!)
    implementation(include("io.github.llamalad7:mixinextras-forge:${project.property("mixinextras_version")}")!!)

    // lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    modRuntimeOnly("curse.maven:jade-324717:6106101")
    modImplementation("appeng:appliedenergistics2-forge:${project.property("ae2_version")}") { isTransitive = false }
    modRuntimeOnly("org.appliedenergistics:guideme:${project.property("guideme_version")}") { isTransitive = false }

    modCompileOnly("dev.architectury:architectury-forge:${project.property("architectury_version")}")
    modCompileOnly("dev.ftb.mods:ftb-library-forge:${project.property("ftb_library_version")}") { isTransitive = false }
    modCompileOnly("dev.ftb.mods:ftb-teams-forge:${project.property("ftb_teams_version")}") { isTransitive = false }

    modImplementation("dev.toma.configuration:configuration-forge-1.20.1:2.2.0")

    // Conditional Mixin support
    implementation("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")
    forgeRuntimeLibrary("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")

    // Kotlin for Forge
//    modImplementation("thedarkcolour:kotlinforforge:${project.property("kff_version")}")
//    forgeRuntimeLibrary("thedarkcolour:kotlinforforge:${project.property("kff_version")}")

    modCompileOnly("mekanism:Mekanism:${project.property("mekanism_version")}:api")
    modImplementation("mekanism:Mekanism:${project.property("mekanism_version")}")
}


tasks.processResources {
    // set up properties for filling into metadata
    val properties = mapOf(
        "mod_license" to project.property("mod_license"),
        "mod_id" to project.property("mod_id"),
        "version" to version,
        "mod_name" to project.property("mod_name"),
        "mod_url" to project.property("mod_url"),
        "mod_author" to project.property("mod_author"),
        "forge_version" to (project.property("forge_version") as String).split(".")[0], // only specify major version of forge
        "minecraft_version" to project.property("minecraft_version"),
        "gtceu_version" to project.property("gtceu_version"),
        "ae2_version" to project.property("ae2_version")
    )
    inputs.properties(properties)

    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }
}

// Copy Kotlin classes to resources/main so Forge can find @Mod classes in dev environment
tasks.register<Copy>("copyKotlinClasses") {
    dependsOn("compileKotlin")
    from(layout.buildDirectory.dir("classes/kotlin/main"))
    into(layout.buildDirectory.dir("resources/main"))
}

tasks.named("processResources") {
    dependsOn("copyKotlinClasses")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}


tasks.jar {
    // add some additional metadata to the jar manifest
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to project.property("mod_id"),
                "Specification-Vendor" to project.property("mod_author"),
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to project.property("mod_author"),
                "Implementation-Timestamp" to System.currentTimeMillis().toString()
            )
        )
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
