import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  val kotlinVersion = "2.3.10"
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.6.0"
  id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
  id("jacoco")
  id("org.sonarqube") version "6.2.0.5505"
  id("org.owasp.dependencycheck") version "12.1.9"
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.jpa") version kotlinVersion
  kotlin("plugin.serialization") version kotlinVersion
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

repositories {
  maven { url = uri("https://repo.spring.io/milestone") }
  mavenCentral()
}

ext["netty.version"] = "4.1.132.Final"
ext["log4j2.version"] = "2.25.4"

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.springframework.boot:spring-boot-starter-jdbc")
  runtimeOnly("org.postgresql:postgresql:42.7.10")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.17.1")
  implementation("io.micrometer:micrometer-registry-prometheus:1.15.10")

  implementation("org.apache.commons:commons-lang3")
  implementation("org.apache.commons:commons-text:1.13.1")
  implementation("commons-codec:commons-codec")
  implementation("com.google.code.gson:gson")
  implementation("org.json:json:20251224")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.awaitility:awaitility-kotlin")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.39")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("javax.xml.bind:jaxb-api:2.3.1")
  testImplementation("io.opentelemetry:opentelemetry-sdk-testing:1.51.0")
  testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  testImplementation("io.mockk:mockk:1.14.9")

  if (project.hasProperty("docs")) {
    implementation("com.h2database:h2")
  }

  constraints {
    implementation("org.webjars:swagger-ui:5.32.1")
  }
}

openApi {
  outputDir.set(project.layout.buildDirectory.dir("docs"))
  outputFileName.set("openapi.json")
  customBootRun.args.set(listOf("--spring.profiles.active=dev,docs"))
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    freeCompilerArgs.addAll("-Xannotation-default-target=param-property")
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
  // https://github.com/mockk/mockk/issues/681
  jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}

tasks.named<BootRun>("bootRun") {
  systemProperty("spring.profiles.active", project.findProperty("profiles")?.toString() ?: "dev")
  systemProperty("server.port", project.findProperty("port")?.toString() ?: "8080")
}

dependencyCheck {
  nvd.datafeedUrl = "file:///opt/vulnz/cache"
}
