//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.architectury.loom") version "1.13.467"
    id("maven-publish")
    kotlin("jvm")
    kotlin("plugin.serialization")
//    id("com.gradleup.shadow") version "9.3.0"
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

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
        output.dir(layout.buildDirectory.dir("classes/java/main"))
    }
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

        create("data") {
            data()

            property("fml.ignoredMods", "kubejs")

            programArgs(
                "--all",
                "--mod", project.property("mod_id") as String,
                "--output", file("src/generated/resources").absolutePath,
                "--existing", file("src/main/resources").absolutePath,
                "--existing-mod", "gtceu",
                "--existing-mod", "gtmthings"
            )

            ideConfigGenerated(true)
            runDir("run/data")
        }
    }

    mods {
        create("gtmqol") {
            sourceSet(sourceSets.main.get())
        }
    }

    forge {
        mixinConfig("gtmqol.mixins.json")
    }
}

repositories {
    mavenLocal()
    flatDir { dir("libs") }
    mavenCentral()
    maven { url = uri("https://maven.firstdarkdev.xyz/snapshots/") }
    maven {
        url = uri("https://maven.gtceu.com")
        content { includeGroup("com.gregtechceu.gtceu") }
    }
    maven { url = uri("https://maven.quiltmc.org/repository/release/") }
    maven { url = uri("https://maven.parchmentmc.org") }
    maven {
        url = uri("https://maven.saps.dev/releases/")
        content { includeGroup("dev.latvian.mods") }
    }
    maven {
        url = uri("https://maven.tterrag.com/")
        content {
            includeGroup("com.jozufozu.flywheel")
            includeGroup("com.tterrag.registrate")
            includeGroup("com.simibubi.create")
        }
    }
    maven { url = uri("https://maven.blamejared.com/") }
    maven { url = uri("https://maven.theillusivec4.top/") }
    maven { url = uri("https://cursemaven.com/") }
    maven { url = uri("https://maven.architectury.dev/") }
    maven { url = uri("https://api.repsy.io/mvn/toma/public/") }
    maven { url = uri("https://api.modrinth.com/maven") }
    maven { url = uri("https://maven.terraformersmc.com/") }
    maven { url = uri("https://modmaven.dev/") }
    maven { url = uri("https://maven.ftb.dev/releases") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://thedarkcolour.github.io/KotlinForForge/") }
    maven {
        url = uri("https://maven.latvian.dev/releases")
        content {
            includeGroup("dev.latvian.mods")
            includeGroup("dev.latvian.apps")
        }
    }

    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.rtyley")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    forge("net.minecraftforge:forge:${project.property("minecraft_version")}-${project.property("forge_version")}")

    // mojmap + parchment mappings
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-${project.property("minecraft_version")}:${project.property("parchment_mappings")}@zip")
    })

    modCompileOnly("mezz.jei:jei-${project.property("minecraft_version")}-forge-api:${project.property("jei_version")}") {
        isTransitive = false
    }
    modCompileOnly("mezz.jei:jei-${project.property("minecraft_version")}-common-api:${project.property("jei_version")}") {
        isTransitive = false
    }
    modCompileOnly("dev.emi:emi-forge:${project.property("emi_version")}:api") { isTransitive = false }

    modImplementation("com.gregtechceu.gtceu:gtceu-${project.property("minecraft_version")}:${project.property("gtceu_version")}") {
        isTransitive = false
    }
    modImplementation("com.lowdragmc.ldlib:ldlib-forge-${project.property("minecraft_version")}:${project.property("ldlib_version")}") {
        isTransitive = false
    }
    modImplementation("com.tterrag.registrate:Registrate:${project.property("registrate_version")}")

    modRuntimeOnly(
        "dev.toma.configuration:configuration-forge-${project.property("minecraft_version")}:${
            project.property(
                "configuration_version"
            )
        }"
    )
    modRuntimeOnly("mezz.jei:jei-${project.property("minecraft_version")}-forge:${project.property("jei_version")}") {
        isTransitive = false
    }

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
    include("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")
    forgeRuntimeLibrary("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")

    // Kotlin for Forge
    forgeRuntimeLibrary("thedarkcolour:kotlinforforge:${project.property("kff_version")}")

    modCompileOnly("mekanism:Mekanism:${project.property("mekanism_version")}:api")
    modImplementation("mekanism:Mekanism:${project.property("mekanism_version")}")

    modApi("dev.latvian.mods:kubejs-forge:${project.property("kubejs_version")}")

    modImplementation(files("libs/gtmthings-1.5.4.jar"))

    modCompileOnly(files("libs/monilabs-0.20.0.jar"))

    implementation("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")

    forgeRuntimeLibrary("com.github.Fallen-Breath.conditional-mixin:conditional-mixin-forge:0.6.4")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val properties = mapOf(
        "mod_license" to project.property("mod_license"),
        "mod_id" to project.property("mod_id"),
        "version" to version,
        "mod_name" to project.property("mod_name"),
        "mod_url" to project.property("mod_url"),
        "mod_author" to project.property("mod_author"),
        "forge_version" to (project.property("forge_version") as String).split(".")[0],
        "minecraft_version" to project.property("minecraft_version"),
        "gtceu_version" to project.property("gtceu_version"),
        "ae2_version" to project.property("ae2_version"),
        "mekanism_version" to project.property("mekanism_version"),
    )
    inputs.properties(properties)

    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }

    doLast {
        val langPath = "assets/gtmqol/lang/en_us.json"
        val generatedFile = file("src/generated/resources/$langPath")
        val manualFile = file("src/main/resources/$langPath")
        val outputDir = layout.buildDirectory.dir("resources/main").get().asFile
        val outputFile = file("$outputDir/$langPath")

        if (generatedFile.exists() && manualFile.exists()) {
            val slurper = JsonSlurper()
            val generatedMap = slurper.parse(generatedFile) as MutableMap<String, Any>
            val manualMap = slurper.parse(manualFile) as Map<String, Any>

            val mergedMap = generatedMap + manualMap

            if (!outputFile.parentFile.exists()) outputFile.parentFile.mkdirs()
            outputFile.writeText(JsonBuilder(mergedMap).toPrettyString())
        }
    }
}

tasks.jar {
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

//    duplicatesStrategy = DuplicatesStrategy.INCLUDE

//    dependsOn(relocateClasses)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/main"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/main"))
}

//val relocateClasses = tasks.register<Sync>("relocateClasses") {
//    group = "build"
//
//    val compileJava = tasks.named<JavaCompile>("compileJava")
//    val compileKotlin = tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin")
//
//    from(compileJava.flatMap { it.destinationDirectory })
//    from(compileKotlin.flatMap { it.destinationDirectory })
//
//    into(layout.buildDirectory.dir("classes/java/main"))
//}

//tasks.named("classes") {
//    dependsOn(relocateClasses)
//}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

sourceSets.main.configure {
    (output.classesDirs as ConfigurableFileCollection).setFrom(layout.buildDirectory.dir("classes/java/main"))
}

tasks.named<Jar>("sourcesJar") {
    exclude { it.file.absolutePath.contains("generated") }
}