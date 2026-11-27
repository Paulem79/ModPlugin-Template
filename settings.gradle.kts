pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}

// Read mod_archive_name from gradle.properties: settings scripts cannot access project properties yet
val rootProps = java.util.Properties().apply {
    file("gradle.properties").inputStream().use { load(it) }
}
rootProject.name = rootProps.getProperty("mod_archive_name")

include("fabric")
include("fabric-legacy")
include("paper")
include("common")
