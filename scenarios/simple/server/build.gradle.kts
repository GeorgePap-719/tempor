plugins {
  kotlin("jvm")
  kotlin("plugin.spring")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

repositories {
  mavenCentral()
}

val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  // Kotlin
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
//  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions") TODO
  // Database
//  runtimeOnly("org.postgresql:postgresql")
//  runtimeOnly("org.postgresql:r2dbc-postgresql")
  // Tests
//  testImplementation("io.projectreactor:reactor-test")
//  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
