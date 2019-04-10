plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.2.1-SNAPSHOT"
}

group = "me.branchpanic.mods"
version = "1.0.0-alpha.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }

    maven(url = "https://maven.jamieswhiteshirt.com/libs-release/") {
        name = "JamiesWhiteShirt Maven"
    }
}

object Versions {
    const val MINECRAFT = "1.14 Pre-Release 1"
    const val YARN = "$MINECRAFT+build.3"
    const val LOADER = "0.4.0+build.119"

    const val FABRIC = "0.2.7+build.122"
    const val FABRIC_KT = "1.3.21-SNAPSHOT"
}

dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings("net.fabricmc:yarn:${Versions.YARN}")
    modCompile("net.fabricmc:fabric-loader:${Versions.LOADER}")

    modCompile("net.fabricmc:fabric:${Versions.FABRIC}")

    include("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")
    modCompile("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")
    compileOnly("net.fabricmc:fabric-language-kotlin:${Versions.FABRIC_KT}")

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

tasks.withType<ProcessResources> {
    filesMatching("fabric.mod.json") {
        expand(Pair("version", project.version))
    }
}
