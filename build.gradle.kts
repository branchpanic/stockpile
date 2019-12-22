import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.2.6-SNAPSHOT"
    id("org.jlleitschuh.gradle.ktlint") version "8.2.0"
}

object Versions {
    // Mod
    const val STOCKPILE = "1.1.3-beta.0"

    // Toolchain dependencies
    const val MINECRAFT = "1.15.1"
    const val YARN = "$MINECRAFT+build.1:v2"
    const val LOADER = "0.7.2+build.175"
    const val FABRIC = "0.4.24+build.279-1.15"

    // Mod dependencies
    const val FABRIC_KT = "1.3.50+build.1"
    const val LBA = "0.5.0"
    const val HWYLA = "1.15-pre4-1.9.19-70"
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

    maven(url = "https://tehnut.info/maven") {
        name = "TehNut"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")

    modCompile("net.fabricmc:fabric-loader:${Versions.LOADER}")

    modCompile("net.fabricmc.fabric-api:fabric-api:${Versions.FABRIC}")
    modCompile("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

    modCompile("alexiil.mc.lib:libblockattributes-all:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-core:${Versions.LBA}")
    include("alexiil.mc.lib:libblockattributes-items:${Versions.LBA}")

    modCompile("mcp.mobius.waila:Hwyla:${Versions.HWYLA}")

    testImplementation("junit:junit:4.12")
    testImplementation("io.kotlintest:kotlintest-runner-junit4:3.4.2")
    testImplementation("org.powermock:powermock-module-junit4:2.0.2")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.2")
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
