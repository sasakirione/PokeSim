plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.0"
}

dependencies {
    implementation(project(":Core"))
    testImplementation(libs.kotlin.test)
    // Ktor client for HTTP requests
    implementation(libs.bundles.ktor.client)
    // Kotlinx serialization for JSON parsing
    implementation(libs.kotlinx.serialization.json)
}

kotlin {
    jvmToolchain(20)
}
