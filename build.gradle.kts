plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.2.0"
  kotlin("plugin.spring") version "2.2.21"
}

configurations {
  implementation { exclude(module = "commons-logging") }
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.8.2")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:6.2.14")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.6.2")

  implementation("org.flywaydb:flyway-database-oracle")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

  runtimeOnly("com.zaxxer:HikariCP")
  implementation("com.oracle.database.jdbc:ojdbc11:23.26.0.0.0")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.8.0.0")

  implementation("org.apache.commons:commons-lang3:3.20.0")
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.19.0")
  implementation("org.jetbrains.exposed:exposed-spring-boot-starter:0.61.0")
  implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.8.2")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

  testImplementation("org.mockito:mockito-inline:5.2.0")

  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.35") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.40")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.21.3")
  testImplementation("org.testcontainers:oracle-xe:1.21.3")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjvm-default=all", "-Xwhen-guards", "-Xannotation-default-target=param-property")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_24
  targetCompatibility = JavaVersion.VERSION_24
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
  }
  test {
    jvmArgs("-Doracle.jakarta.jms.useEmulatedXA=false")
  }
}
