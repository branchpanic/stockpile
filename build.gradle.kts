import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.2.5-SNAPSHOT"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
}

object Versions {
    // When updating Stockpile's version, just change this constant. The MC version in the metadata is automatically
    // added.
    const val STOCKPILE = "1.0.9"

    const val MINECRAFT = "1.14.4"
    const val YARN = "$MINECRAFT+build.2"
    const val LOADER = "0.4.8+build.157"

    const val FABRIC = "0.3.0+build.200"
    const val FABRIC_KT = "1.3.30+build.2"
    const val LBA = "0.4.9"

    const val SIMPLE_PIPES = "0.1.8"
}

group = "me.branchpanic.mods"
version = Versions.STOCKPILE + "+" + Versions.MINECRAFT.replace(" Pre-Release ", "-Pre")

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
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")

    modCompile("net.fabricmc:fabric-loader:${Versions.LOADER}")

    // Mod dependencies
    modCompile("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC}")
    modCompile("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

    modCompile("alexiil.mc.lib:libblockattributes-all:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-core:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-items:${Versions.LBA}")

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