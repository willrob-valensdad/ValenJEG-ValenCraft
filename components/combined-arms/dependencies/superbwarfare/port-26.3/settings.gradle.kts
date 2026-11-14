pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
    plugins {
        id("fabric-loom") version "1.18.2"
        id("org.jetbrains.kotlin.jvm") version "2.3.10"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
        id("com.google.devtools.ksp") version "2.3.10"
    }
}

include(":ksp")
