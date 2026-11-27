import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.modrinth.minotaur.ModrinthExtension
import org.gradle.api.GradleException
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.io.File
import java.nio.ByteBuffer

plugins {
    java
    id("com.gradleup.shadow") version "9.+"
    id("com.modrinth.minotaur") version "2.+"
}

allprojects {
    group = project.property("maven_group") as String
    version = project.property("mod_version") as String

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://api.modrinth.com/maven") {
            content { includeGroup("maven.modrinth") }
        }
    }
}

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile>().configureEach {
        options.release.set((project.property("java_new") as String).toInt())
    }
}

// Fabric new (unobfuscated Minecraft)
tasks.shadowJar {
    archiveClassifier.set("fabric")

    from(project(":common").sourceSets.main.get().output)
    from(project(":fabric").sourceSets.main.get().output)

    configurations = emptyList()

    mergeServiceFiles()
}

// Fabric legacy (obfuscated Minecraft 1.21.x, Java 21)
val legacyShadowJar by tasks.registering(ShadowJar::class) {
    val legacyRemapJar = project(":fabric-legacy").tasks.named<AbstractArchiveTask>("remapJar")
    dependsOn(legacyRemapJar)

    archiveClassifier.set("fabric-mc1.21")

    from(project(":common").sourceSets.main.get().output)
    from(legacyRemapJar.map { zipTree(it.archiveFile) })

    configurations = emptyList()

    mergeServiceFiles()
}

// Paper plugin (Paper, Purpur, Folia).
// Resolved lazily because the paper project is configured after this one.
val paperJar = provider { project(":paper").tasks.named<AbstractArchiveTask>("shadowJar").get() }

tasks.build {
    dependsOn(tasks.shadowJar, legacyShadowJar, paperJar)
}

// Gradle reads gradle.properties as ISO-8859-1; re-decode values as UTF-8 so accented changelogs work
fun asUtf8(value: String): String {
    if (value.any { it.code > 255 }) {
        return value
    }
    val bytes = ByteBuffer.wrap(value.toByteArray(Charsets.ISO_8859_1))
    return runCatching { Charsets.UTF_8.newDecoder().decode(bytes).toString() }.getOrDefault(value)
}

fun ModrinthExtension.configureCommon(loadersProperty: String, gameVersions: List<String>, requireFabricDependencies: Boolean) {
    token.set(providers.gradleProperty("MODRINTH_TOKEN"))
    projectId.set(project.property("modrinth_id") as String)
    versionType.set(project.property("modrinth_version_type") as String)
    providers.gradleProperty("modrinth_changelog").orNull?.takeIf { it.isNotBlank() }?.let { changelog.set(asUtf8(it)) }
    loaders.addAll((project.property(loadersProperty) as String).split(","))
    this.gameVersions.addAll(gameVersions)
    if (requireFabricDependencies) {
        dependencies {
            required.project("fabric-api")
        }
    }
}

fun versionsOf(key: String) = (project.property(key) as String).split(",")

modrinth {
    configureCommon("modrinth_fabric_loaders", versionsOf("modrinth_game_versions"), true)
    versionNumber.set("$version+fabric")
    versionName.set("${project.property("mod_name")} $version (Fabric)")
    uploadFile.set(tasks.shadowJar)
    syncBodyFrom.set(providers.fileContents(layout.projectDirectory.file("README.md")).asText)
}

tasks.named("modrinth") {
    dependsOn(tasks.shadowJar)
}

project(":fabric-legacy") {
    apply(plugin = "com.modrinth.minotaur")

    configure<ModrinthExtension> {
        configureCommon("modrinth_fabric_loaders", versionsOf("modrinth_legacy_game_versions"), true)
        versionNumber.set("$version+fabric.mc1.21")
        versionName.set("${project.property("mod_name")} $version (Fabric, MC 1.21.x)")
        uploadFile.set(legacyShadowJar)
    }

    tasks.named("modrinth") {
        dependsOn(legacyShadowJar)
    }

    // The root project already syncs the description
    tasks.named("modrinthSyncBody") {
        enabled = false
    }
}

project(":paper") {
    apply(plugin = "com.modrinth.minotaur")

    configure<ModrinthExtension> {
        configureCommon(
            "modrinth_paper_loaders",
            versionsOf("modrinth_paper_extra_game_versions") + versionsOf("modrinth_legacy_game_versions") + versionsOf("modrinth_game_versions"),
            false
        )
        versionNumber.set("$version+paper")
        versionName.set("${project.property("mod_name")} $version (Paper)")
        uploadFile.set(paperJar)
    }

    tasks.named("modrinth") {
        dependsOn(paperJar)
    }

    tasks.named("modrinthSyncBody") {
        enabled = false
    }
}

// ./gradlew initProject: run ONCE after copying the template.
// Applies base_package, class_prefix, mod_id and mod_name from gradle.properties
// to the code: file contents, directories and Java file names.
// Does nothing once the template defaults are gone from the sources.
tasks.register("initProject") {
    group = "template"
    description = "Applies base_package, class_prefix, mod_id and mod_name from gradle.properties to the code"

    doLast {
        val basePackage = project.property("base_package") as String
        val classPrefix = project.property("class_prefix") as String
        val modId = project.property("mod_id") as String
        val modName = project.property("mod_name") as String

        // Template defaults
        val tplPackage = "com.example.template"
        val tplPrefix = "Template"
        val tplId = "template"
        val tplName = "Template Mod"

        val moduleNames = listOf("common", "fabric", "paper")

        // Step 1: replace through placeholders so one replacement cannot be rewritten by another
        // (e.g. a package containing "template")
        val toMarkers = listOf(tplPackage to "@@PKG@@", tplName to "@@NAME@@", tplPrefix to "@@PREFIX@@", tplId to "@@ID@@")
        val fromMarkers = listOf("@@PKG@@" to basePackage, "@@NAME@@" to modName, "@@PREFIX@@" to classPrefix, "@@ID@@" to modId)

        moduleNames.forEach { module ->
            project.fileTree(project.file("$module/src")) {
                include("**/*.java", "**/*.json", "**/*.yml")
            }.files.forEach { f ->
                var text = f.readText()
                val original = text
                toMarkers.forEach { (from, to) -> text = text.replace(from, to) }
                fromMarkers.forEach { (from, to) -> text = text.replace(from, to) }
                if (text != original) f.writeText(text)
            }
        }

        // Step 2: move directories (Java package, then assets)
        fun moveDir(from: File, to: File, stopAt: File) {
            if (from == to || !from.exists()) return
            if (to.exists()) throw GradleException("Cannot move $from: $to already exists")
            to.parentFile.mkdirs()
            if (!from.renameTo(to)) throw GradleException("Failed to move $from to $to")
            var parent = from.parentFile
            while (parent != stopAt && parent.isDirectory && parent.listFiles().isNullOrEmpty()) {
                parent.delete()
                parent = parent.parentFile
            }
        }

        moduleNames.forEach { module ->
            val javaRoot = project.file("$module/src/main/java")
            moveDir(File(javaRoot, tplPackage.replace('.', '/')), File(javaRoot, basePackage.replace('.', '/')), javaRoot)
        }
        val assets = project.file("fabric/src/main/resources/assets")
        moveDir(File(assets, tplId), File(assets, modId), assets)

        // Step 3: rename Java files carrying the prefix
        if (classPrefix != tplPrefix) {
            moduleNames.forEach { module ->
                project.fileTree(project.file("$module/src")) {
                    include("**/*$tplPrefix*.java")
                }.files.forEach { f ->
                    f.renameTo(File(f.parentFile, f.name.replace(tplPrefix, classPrefix)))
                }
            }
        }

        logger.lifecycle("initProject done: package=$basePackage, prefix=$classPrefix, mod_id=$modId, name=$modName")
    }
}
