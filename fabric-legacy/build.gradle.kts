plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.19.4"
    java
}

// Same source code as the fabric module (sources are shared)
sourceSets.main {
    java.srcDir("../fabric/src/main/java")
    resources.srcDir("../fabric/src/main/resources")
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:${project.property("legacy_minecraft")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("legacy_fabric_api")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set((project.property("java_legacy") as String).toInt())
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "mod_id" to project.property("mod_id"),
        "mod_name" to project.property("mod_name"),
        "description" to project.property("mod_description"),
        "author" to project.property("author"),
        "author_website" to project.property("author_website"),
        "source_url" to project.property("source_url"),
        "issues_url" to project.property("issues_url"),
        "license" to project.property("license"),
        "modrinth_id" to project.property("modrinth_id"),
        "entrypoint" to "${project.property("base_package")}.fabric.Fabric${project.property("class_prefix")}",
        "minecraft" to project.property("legacy_mc_range"),
        "java" to ">=${project.property("java_legacy")}"
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
