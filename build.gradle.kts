plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("com.natpryce:hamkrest:1.7.0.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    register<Test>("inMemory") {
        environment("DDT_CONFIG", "in-memory")
    }

    register<Test>("cli") {
        environment("DDT_CONFIG", "cli")
    }
}

kotlin {
    jvmToolchain(21)
}