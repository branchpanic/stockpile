import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    id("fabric-loom") version "0.9-SNAPSHOT"
}

object Versions {
    // Mod
    const val STOCKPILE = "2.0.0-experimental"

    // Toolchain dependencies
    const val MINECRAFT = "1.17.1"
    const val YARN = "$MINECRAFT+build.59"
    const val LOADER = "0.11.6"
    const val FABRIC = "0.40.0+1.17"

    // Mod dependencies
    const val FABRIC_KT = "1.6.4+kotlin.1.5.30"
    const val LBA = "0.9.0"
    const val WTHIT = "3.9.0"
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

    maven(url = "https://maven.bai.lol")
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")

    modImplementation("net.fabricmc:fabric-loader:${Versions.LOADER}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

    modImplementation("alexiil.mc.lib:libblockattributes-all:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-all:${Versions.LBA}")

    modRuntime("mcp.mobius.waila:wthit:fabric-${Versions.WTHIT}")
    modCompileOnly("mcp.mobius.waila:wthit-api:fabric-${Versions.WTHIT}")
}

loom {
    accessWidenerPath.set(file("src/main/resources/stockpile.accesswidener"))
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
    kotlinOptions.jvmTarget = "16"
}
