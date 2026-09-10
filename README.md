# Fabric + NeoForge Mod Template

A minimal multi-loader Minecraft mod workspace based on the structure used by
Endernium. Shared Java code and resources live in `common`; each loader owns its
bootstrap code, build configuration, metadata, and generated resources.

## Requirements

- Minecraft 26.2
- Java 25
- An IDE with Gradle support (IntelliJ IDEA or VS Code are both suitable)

The Gradle JVM itself should also be Java 25. In particular, NeoForge's Gradle
9.2.1 wrapper will fail early with `Unsupported class file major version 70` if
it is launched by Java 26. Set your IDE's Gradle JVM or `JAVA_HOME` to a Java 25
installation before importing or running tasks.

## Project layout

```text
common/    Loader-neutral main/client code, assets, data, and tests
fabric/    Fabric entry points, metadata, build, runs, and data generation
neoforge/  NeoForge entry points, metadata template, build, runs, and data generation
```

The Fabric and NeoForge directories are independent Gradle projects. Both add
`../common/src/main/java`, `../common/src/main/resources`, and
`../common/src/test/java` to their source sets.

Fabric uses Loom's split environment source sets, so code under
`common/src/client` and `fabric/src/client` cannot accidentally leak into the
dedicated-server compile classpath. NeoForge packages shared client classes but
only initializes them through its client-only `@Mod` entry point.

## Create a mod from this template

The quickest setup is the configuration script:

```powershell
./scripts/configure-template.ps1 `
  -ModId samplemod `
  -ModName "Sample Mod" `
  -Package dev.yourname.samplemod `
  -ClassName SampleMod `
  -Author "Your Name" `
  -Description "What the mod does."
```

The script validates the identifiers, updates metadata and source contents,
moves all Java packages, renames the entry-point classes, and renames the VS
Code workspace. Run it once, before adding your own code.

To configure the project manually instead:

1. Replace every occurrence of `thestaff`, `The Staff`, `TheStaff`,
   `Skittlq`, and `com.skittlq.thestaff` with your own values.
2. Rename the Java package directories under `common`, `fabric`, and `neoforge`.
3. Rename `template.code-workspace` if desired.
4. Choose a license and replace `LICENSE`.
5. Update dependency versions independently in both `gradle.properties` files
   when changing Minecraft versions.
6. Import `fabric` and `neoforge` as Gradle projects, or open
   `template.code-workspace` in VS Code.

Keep Minecraft-facing code in `common` when the API is identical on both
loaders. Put event registration, loader APIs, and other platform-specific code
in the corresponding loader directory. Keep all rendering, screens, keybinds,
and other client-only code in a `src/client` directory where possible.

The VS Code workspace recommends the Java and Gradle extensions and provides
tasks for both clients, both data generators, and tests across both loaders.
Use **Terminal > Run Task** to access them.

## Continuous integration

`.github/workflows/validate.yml` builds and tests Fabric and NeoForge separately
on Java 25 for every push and pull request. It also validates both Gradle wrapper
JARs and caches Gradle dependencies. Delete or adjust the workflow if you do not
host the project on GitHub.

## Useful commands

From `fabric`:

```shell
./gradlew runClient
./gradlew runServer
./gradlew test
./gradlew build
```

From `neoforge`:

```shell
./gradlew runClient
./gradlew runServer
./gradlew test
./gradlew build
```

On Windows, use `gradlew.bat` instead of `./gradlew`.
