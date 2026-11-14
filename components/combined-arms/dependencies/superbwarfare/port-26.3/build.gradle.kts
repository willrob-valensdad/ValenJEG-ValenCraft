plugins {
    idea
    id("java-library")
    id("fabric-loom")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    `maven-publish`
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
}

version = "${project.property("minecraft_version")}-${project.property("mod_version")}"
group = "com.atsuishio.superbwarfare"

repositories {
    mavenCentral()
    mavenLocal()

    flatDir {
        dirs("libs")
    }

    maven("https://maven.fabricmc.net/")

    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }

    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
    }

    maven("https://maven.shedaniel.me") {
        name = "Shedaniel"
    }

    maven("https://maven.createmod.net") {
        name = "CreateMod"
    }

    maven("https://maven.ryanhcode.dev/releases") {
        name = "RyanHCode"
    }

    maven("https://maven.ladysnake.org/releases") {
        name = "Ladysnake"
    }

    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") {
        name = "Fuzs Mod Resources"
    }

    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }

    maven("https://jitpack.io") {
        name = "JitPack"
    }

    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }

    maven("https://maven.blamejared.com/") {
        name = "BlameJared"
        content {
            includeGroup("mezz.jei")
            includeGroup("vazkii.patchouli")
        }
    }

    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

base {
    archivesName.set(project.property("mod_id") as String)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

loom {
    accessWidenerPath = file("src/main/resources/superbwarfare.accesswidener")
    mods {
        create(project.property("mod_id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

val labeledMods = file(providers.gradleProperty("valen_api_inputs").getOrElse("compile-inputs"))

dependencies {
    add("ksp", project(":ksp"))
    implementation(project(":ksp"))

    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    // 26.3 ships named classes and publishes no client_mappings download.
    mappings(files("mappings/identity.jar"))

    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    // Compile against Fabric API 0.165.0+26.3. The pack label stays on the root-prepared
    // fabric-api-0.165.0+26.3-valenjeg-label.jar; that jar is a nested bundle and is not rewritten here.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.165.0+26.3")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}") {
        isTransitive = false
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    // GeckoLib 5.5.7 is the 26.3 build. 4.7.5 is an intermediary 1.21.1 jar and cannot be remapped onto official 26.3.
    modImplementation(files(labeledMods.resolve("geckolib-fabric-26.3-5.5.7.jar")))
    modImplementation(files(rootProject.file("../../tacz/TACZ-Refabricated-26.3-1.1.8+fabric.26.3.R1-hotfix3.jar")))
    modCompileOnly("mezz.jei:jei-26.3-fabric:31.17.0.93") {
        isTransitive = false
    }
    modCompileOnly(files(labeledMods.resolve("Jade-mc26.3-Fabric-26.3.5.jar")))

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    runtimeOnly("com.google.code.findbugs:jsr305:3.0.2")
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("src/generated/resources")
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.WARN
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("minecraft_version"),
        "loader_version" to project.property("loader_version"),
        "fabric_version" to project.property("fabric_version"),
        "mod_id" to project.property("mod_id"),
        "mod_name" to project.property("mod_name"),
        "mod_license" to project.property("mod_license"),
        "mod_version" to project.property("mod_version"),
        "mod_authors" to project.property("mod_authors"),
        "mod_description" to project.property("mod_description")
    )

    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/repo")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

kotlin {
    jvmToolchain(25)
    sourceSets {
        named("main") {
            kotlin.srcDirs("src/main/kotlin", "src/main/java")
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
