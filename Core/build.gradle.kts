plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
}

dependencies {
    testImplementation(libs.kotlin.test)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.arrow)
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.testng)
}
