plugins {
    kotlin("jvm") version "2.3.0"
}

dependencies {
    implementation(libs.clikt)
    implementation(project(":Core"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(20)
}
