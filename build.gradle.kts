plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlinx.atomicfu") version "0.26.1"
}

group = "github.io"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

//dependencies {
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
//    testImplementation(kotlin("test"))
//}

//tasks.test {
//    useJUnitPlatform()
//}
//kotlin {
//    jvmToolchain(17)
//}

// kotlin {
//  compilerOptions {
//    freeCompilerArgs.addAll("-Xjsr305=strict")
//  }
//}
//
//tasks.withType<Test> {
//  useJUnitPlatform()
//}

allprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            // Only execute on the outermost suite.
            if (desc.parent == null) {
                println("Tests: ${result.testCount}")
                println("Passed: ${result.successfulTestCount}")
                println("Failed: ${result.failedTestCount}")
                println("Skipped: ${result.skippedTestCount}")
            }
        }))
    }
}