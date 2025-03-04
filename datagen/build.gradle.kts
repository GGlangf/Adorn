plugins {
    // The Kotlin DSL plugin sets up Kotlin with the correct version
    // and lets me do .gradle.kts files as plugins.
    // See https://docs.gradle.org/current/userguide/kotlin_dsl.html#sec:kotlin-dsl_plugin
    `kotlin-dsl`
    id("org.jmailen.kotlinter") version "3.2.0"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
