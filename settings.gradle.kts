pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
  }

  resolutionStrategy {
    plugins {
      val kotlinVersion = extra["kotlinVersion"] as String
      val kotlinxAtomicfu = extra["kotlinxAtomicfu"] as String
      val springBootVersion = extra["springBootVersion"] as String
      val springDependencyManagement = extra["springDependencyManagement"] as String

      kotlin("jvm") version kotlinVersion apply false
      kotlin("plugin.serialization") version kotlinVersion apply false
      kotlin("plugin.spring") version kotlinVersion apply false
      id("org.jetbrains.kotlinx.atomicfu") version kotlinxAtomicfu apply false
      id("org.springframework.boot") version springBootVersion apply false
      id("io.spring.dependency-management") version springDependencyManagement apply false
    }
  }
}

rootProject.name = "tempor"

fun module(name: String, path: String) {
  include(name)
  val projectDir = rootDir.resolve(path).normalize().absoluteFile
  if (!projectDir.exists()) {
    throw AssertionError("file $projectDir does not exist")
  }
  project(name).projectDir = projectDir
}

module(":tempor-core", "tempor-core")
module(":server", "scenarios/simple/server")