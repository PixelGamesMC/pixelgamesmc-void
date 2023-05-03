import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"

    kotlin("plugin.serialization") version "1.8.10"
}

group = "eu.pixelgamesmc"
version = "1.0-SNAPSHOT"

val pixelUsername: String by project
val pixelPassword: String by project

repositories {
    maven {
        credentials {
            username = pixelUsername
            password = pixelPassword
        }

        url = uri("https://repository.pixelgamesmc.eu/releases")
    }
}

dependencies {
    testImplementation(kotlin("test"))

    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("org.litote.kmongo:kmongo:4.8.0")
    implementation("org.litote.kmongo:kmongo-serialization:4.8.0")
    implementation("org.litote.kmongo:kmongo-id-serialization:4.8.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}