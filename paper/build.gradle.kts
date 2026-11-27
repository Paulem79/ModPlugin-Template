plugins {
    java
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper") version "3.+"
}

// Compiled against the oldest supported API so the plugin also runs on newer versions.
// Override with -PpaperApi=<version>.
val paperApi = providers.gradleProperty("paperApi").getOrElse(project.property("paper_api") as String)

dependencies {
    implementation(project(":common"))
    implementation("org.bstats:bstats-bukkit:${project.property("bstats_version")}")
    compileOnly("io.papermc.paper:paper-api:$paperApi")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set((project.property("java_legacy") as String).toInt())
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "mod_id" to project.property("mod_id"),
            "mod_name" to project.property("mod_name"),
            "description" to project.property("mod_description"),
            "author" to project.property("author"),
            "author_website" to project.property("author_website"),
            "main" to "${project.property("base_package")}.paper.Paper${project.property("class_prefix")}"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveClassifier.set("plain")
    }

    shadowJar {
        archiveBaseName.set("${project.property("mod_archive_name")}-paper")
        archiveClassifier.set("")

        from(project(":common").sourceSets.main.get().output)

        configurations = project.configurations.runtimeClasspath.map { setOf(it) }

        dependencies {
            // Only bStats is embedded in the final jar
            exclude { it.moduleGroup != "org.bstats" }
        }

        // Relocated to avoid conflicts with other plugins shipping bStats
        relocate("org.bstats", "${project.property("base_package")}.paper.libs.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(project.property("paper_run_version") as String)
    }
}
