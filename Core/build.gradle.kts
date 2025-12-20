plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

dependencies {
    testImplementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.testng)
}
