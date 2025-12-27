plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
}

version = rootProject.property("mod_version")!!
group = rootProject.property("maven_group")!!

base {
    archivesName.set(rootProject.property("archives_base_name")!!.toString())
}

repositories {
    maven("https://maven.terraformersmc.com/")
}

val minecraft = project.property("minecraft_version")!!
val loader = project.property("loader_version")!!
val modMenuVer = project.property("modmenu_version")!!

dependencies {
    minecraft("com.mojang:minecraft:${minecraft}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${loader}")
    modCompileOnly("com.terraformersmc:modmenu:${modMenuVer}")
}

tasks {
    processResources {
        inputs.property("version", version)
        inputs.property("minecraft_version", minecraft)
        inputs.property("loader_version", loader)

        filesMatching("fabric.mod.json") {
            expand(
                "version" to version,
                "minecraft_version" to minecraft,
                "loader_version" to loader,
                "modmenu_version" to modMenuVer
            )
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}