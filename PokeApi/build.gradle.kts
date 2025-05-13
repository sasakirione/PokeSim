plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "com.sasakirione"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Core"))
    testImplementation(kotlin("test"))
    // Ktor client for HTTP requests
    implementation("io.ktor:ktor-client-core:3.1.3")
    implementation("io.ktor:ktor-client-cio:3.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")

    // Kotlinx serialization for JSON parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)
}