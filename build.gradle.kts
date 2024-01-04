plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.13.0"
  kotlin("plugin.spring") version "1.9.22"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:6.1.2")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:2.1.1")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-cache")

  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-oracle")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

  implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.3.0.0")

  implementation("org.apache.commons:commons-lang3:3.14.0")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

  testImplementation("org.mockito:mockito-inline:5.2.0")

  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.19") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.20")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.19.3")
  testImplementation("org.testcontainers:oracle-xe:1.19.3")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
  test {
    jvmArgs("-Doracle.jakarta.jms.useEmulatedXA=false")
  }
}
