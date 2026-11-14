pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
    }
}

rootProject.name = "hbm-valen"

include("processor")
include("fabric")
include("neoforge")
