plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.0"
}

dependencies {
    testImplementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.testng)
}
