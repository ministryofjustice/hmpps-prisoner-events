plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.4"
  kotlin("plugin.spring") version "2.0.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.5")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:6.1.12")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.4.0")

  implementation("org.flywaydb:flyway-database-oracle")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

  implementation("com.zaxxer:HikariCP:5.1.0")
  implementation("com.oracle.database.jdbc:ojdbc11:23.5.0.24.07")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.3.1.0")

  implementation("org.apache.commons:commons-lang3:3.16.0")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.6.0")
  implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.53.0")
  implementation("org.jetbrains.exposed:exposed-java-time:0.53.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.5")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.2")

  testImplementation("org.mockito:mockito-inline:5.2.0")

  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.22") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.22")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.20.1")
  testImplementation("org.testcontainers:oracle-xe:1.20.1")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
  test {
    jvmArgs("-Doracle.jakarta.jms.useEmulatedXA=false")
  }
}
