plugins {
    kotlin("jvm") version "2.4.21"
}

dependencies {
    implementation(libs.clikt)
    implementation(project(":Core"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.arrow)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(20)
}
