# Minecraft mod / plugin template (Fabric + Paper)

Reusable project base: one shared module, two Fabric modules (new and legacy) and a Paper / Purpur / Folia plugin, published on Modrinth.

```
common/        shared logic, no loader dependency
fabric/        Fabric mod, unobfuscated Minecraft (Java 25)
fabric-legacy/ Fabric mod, obfuscated Minecraft 1.21.x (Java 21), same sources as fabric/
paper/         Paper / Purpur / Folia plugin (Java 21)
```

## Starting a new project

1. Copy this folder and edit **`gradle.properties`**: Maven group, name, author, Minecraft versions, Modrinth ids, etc.
2. Set the code identity: `mod_id`, `base_package`, `class_prefix`.
3. Run **once**:

   ```sh
   ./gradlew initProject
   ```

   This renames the Java package, the classes (`TemplateConfig` becomes `<class_prefix>Config`, etc.), the directories and the Fabric asset folder, using the values in `gradle.properties`. It does nothing if the template defaults are already gone.

   > After `initProject`, `mod_id`, `base_package` and `class_prefix` should not change: they are baked into the code. All other properties can be edited at any time.

4. Replace `fabric/src/main/resources/assets/<mod_id>/icon.png` with your icon.

## Commands

| Command | Effect |
| --- | --- |
| `./gradlew build` | Builds the three jars into `build/libs` |
| `./gradlew :paper:runServer` | Starts a test Paper server |
| `./gradlew :fabric:runClient` | Starts Minecraft (Fabric new) |
| `./gradlew modrinth` | Publishes to Modrinth (requires `MODRINTH_TOKEN`) |

## `gradle.properties` reference

| Group | Properties | Used by |
| --- | --- | --- |
| Identity | `maven_group`, `mod_version`, `mod_name`, `mod_archive_name`, `mod_description`, `author`, `author_website`, `source_url`, `issues_url`, `license` | Read on every build, injected into `fabric.mod.json` and `plugin.yml` |
| Code (one-time) | `mod_id`, `base_package`, `class_prefix` | `initProject` |
| Modrinth | `modrinth_id`, `modrinth_*` | Publishing |
| Fabric new | `new_minecraft`, `new_fabric_api`, `new_mc_range`, `java_new` | `fabric` module |
| Fabric legacy | `legacy_minecraft`, `legacy_fabric_api`, `legacy_mc_range`, `java_legacy` | `fabric-legacy` module, and `common` |
| Shared | `fabric_loader_version` | Fabric modules |
| Paper | `paper_api`, `paper_run_version`, `bstats_version` | `paper` module |

Gradle plugin versions (Loom, Shadow, Minotaur, run-paper) are written directly in the `build.gradle.kts` files: Gradle does not allow reading them from `gradle.properties` inside the `plugins {}` block.

## Configuration

Fabric builds use the defaults from `TemplateConfig.DEFAULTS` (no config UI). The Paper plugin reads `config.yml`.

## License

Set `license` in `gradle.properties`, and add a `LICENSE` file that matches it.
