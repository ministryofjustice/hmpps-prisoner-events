plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.5.1"
  kotlin("plugin.spring") version "1.9.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:6.0.12")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.1.1")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-cache")

  implementation("org.flywaydb:flyway-core")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

  implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.2.1.0")

  implementation("org.apache.commons:commons-lang3:3.13.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.2")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.2")

  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.16")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.19.1")
  testImplementation("org.testcontainers:oracle-xe:1.19.1")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(20))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "20"
    }
  }
  test {
    jvmArgs("-Doracle.jakarta.jms.useEmulatedXA=false")

    // required for jjwt 0.12 - see https://github.com/jwtk/jjwt/issues/849
    jvmArgs("--add-exports", "java.base/sun.security.util=ALL-UNNAMED")
  }
}
