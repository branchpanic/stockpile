import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.2.2-SNAPSHOT"
}

object Versions {
    // When updating Stockpile's version, just change this constant. The MC version in the metadata is automatically
    // added.
    const val STOCKPILE = "1.0.6"

    // This tag is used to differentiate Stockpile builds, and should generally correspond with the current branch OR
    // be empty for full releases.
    const val STOCKPILE_TAG = ""

    const val MINECRAFT = "1.14.1"
    const val YARN = "$MINECRAFT+build.5"
    const val LOADER = "0.4.7+build.147"

    const val FABRIC = "0.3.0-pre+build.156"
    const val FABRIC_KT = "1.3.30+build.2"
    const val MOD_MENU = "1.4.0-72"
    const val REI = "2.7.11.97"
    const val CLOTH_CONFIG = "0.1.3.7"
    const val CLOTH_EVENTS = "0.3.1.23"
}

group = "me.branchpanic.mods"
version = Versions.STOCKPILE +
        if (Versions.STOCKPILE_TAG.isNotBlank()) {
            "-${Versions.STOCKPILE_TAG}"
        } else {
            ""
        } +
        "+" + Versions.MINECRAFT.replace(" Pre-Release ", "-Pre")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }

    maven(url = "https://maven.jamieswhiteshirt.com/libs-release/") {
        name = "JamiesWhiteShirt"
    }

    maven(url = "https://minecraft.curseforge.com/api/maven") {
        name = "CurseForge"
    }

    maven(url = "https://mod-buildcraft.com/maven") {
        name = "BuildCraft"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")
    modCompile("net.fabricmc:fabric-loader:${Versions.LOADER}")

    // Mod dependencies
    modCompile("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC}")
    modCompile("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")
    compileOnly("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

    modCompile("alexiil.mc.lib:libblockattributes:0.4.2") { isTransitive = false }
    include("alexiil.mc.lib:libblockattributes:0.4.2")

    // Additional dev environment mods (rip)
    // modCompile("io.github.prospector.modmenu:ModMenu:${Versions.MOD_MENU}")
    // modCompile("roughly-enough-items:RoughlyEnoughItems:${Versions.REI}")
    // modCompile("cloth-config:ClothConfig:${Versions.CLOTH_CONFIG}")
    // modCompile("cloth:ClothEvents:${Versions.CLOTH_EVENTS}")

    testImplementation("junit:junit:4.12")
    testImplementation("io.kotlintest:kotlintest-runner-junit4:3.3.2")
    testImplementation("org.powermock:powermock-module-junit4:1.7.1")
    testImplementation("org.powermock:powermock-api-mockito2:1.7.1")
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