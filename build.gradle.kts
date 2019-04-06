plugins {
    kotlin("jvm") version "1.3.21"
    id("fabric-loom") version "0.2.0-SNAPSHOT"
}

group = "me.branchpanic.mods"
version = "1.0.0-alpha.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    minecraft("com.mojang:minecraft:19w14b")
    mappings("net.fabricmc:yarn:19w14b.1")
    modCompile("net.fabricmc:fabric-loader:0.3.7.109")

    modCompile("net.fabricmc:fabric:0.2.6.119")
    modCompile("net.fabricmc:fabric-language-kotlin:1.3.21-SNAPSHOT")
    compileOnly("net.fabricmc:fabric-language-kotlin:1.3.21-SNAPSHOT")

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
