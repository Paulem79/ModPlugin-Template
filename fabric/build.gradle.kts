plugins {
    id("net.fabricmc.fabric-loom") version "1.19.6"
    java
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:${project.property("new_minecraft")}")
    implementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("new_fabric_api")}")
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
        "minecraft" to project.property("new_mc_range"),
        "java" to ">=${project.property("java_new")}"
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}
