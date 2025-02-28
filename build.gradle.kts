plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "7.1.3"
  kotlin("plugin.spring") version "2.1.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.3.1")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:6.2.3")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.3.1")

  implementation("org.flywaydb:flyway-database-oracle")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

  runtimeOnly("com.zaxxer:HikariCP")
  implementation("com.oracle.database.jdbc:ojdbc11:23.7.0.25.01")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.3.1.0")

  implementation("org.apache.commons:commons-lang3:3.17.0")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.12.0")
  implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.59.0")
  implementation("org.jetbrains.exposed:exposed-java-time:0.59.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.3.1")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

  testImplementation("org.mockito:mockito-inline:5.2.0")

  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.25") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.28")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.20.5")
  testImplementation("org.testcontainers:oracle-xe:1.20.5")
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
