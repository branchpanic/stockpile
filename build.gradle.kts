import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.4-SNAPSHOT"
}

object Versions {
    // Mod
    const val STOCKPILE = "1.1.5"

    // Toolchain dependencies
    const val MINECRAFT = "1.16.1"
    const val YARN = "$MINECRAFT+build.21:v2"
    const val LOADER = "0.9.0+build.204"
    const val FABRIC = "0.16.0+build.384-1.16.1"

    // Mod dependencies
    const val FABRIC_KT = "1.3.72+build.1"
    const val LBA = "0.7.0"
    const val HWYLA = "1.16.1-1.9.22-75"
}

group = "me.branchpanic.mods"
version = Versions.STOCKPILE + "+" + Versions.MINECRAFT

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()

    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }

    maven(url = "https://mod-buildcraft.com/maven") {
        name = "BuildCraft"
    }

    maven(url = "https://maven.tehnut.info/") {
        name = "TehNut"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")

    modImplementation("net.fabricmc:fabric-loader:${Versions.LOADER}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

    modImplementation("alexiil.mc.lib:libblockattributes-all:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-core:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-items:${Versions.LBA}")

    modImplementation("mcp.mobius.waila:Hwyla:${Versions.HWYLA}")
}

task("sourcesJar", Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
    dependsOn(tasks.getByName("classes"))
}

tasks.withType<ProcessResources>().all {
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}
