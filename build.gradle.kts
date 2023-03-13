plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.4-beta-2"
  kotlin("plugin.spring") version "1.8.10"
  id("org.unbroken-dome.test-sets") version "4.0.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

testSets {
  "testSmoke"()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.amazonaws:aws-java-sdk-sns")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:1.2.0")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  implementation("org.flywaydb:flyway-core")

  implementation("org.springdoc:springdoc-openapi-ui:1.6.14")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.14")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.14")

  implementation("com.oracle.database.jdbc:ojdbc11:21.9.0.0")
  implementation("com.oracle.database.messaging:aqapi:21.3.0.0")

  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("org.mockito:mockito-inline:5.1.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.12")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.17.6")
  testImplementation("org.testcontainers:oracle-xe:1.17.6")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}
repositories {
  mavenCentral()
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }
  }
}
