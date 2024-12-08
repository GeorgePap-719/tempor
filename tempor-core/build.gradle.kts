plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.atomicfu")
}

repositories {
    mavenCentral()
}

val kotlinxCoroutinesVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}