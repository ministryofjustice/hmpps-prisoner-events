plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.3"
  kotlin("plugin.spring") version "2.3.0"
}

dependencies {
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.0.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework:spring-jms:7.0.3")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.0.0-beta2")
  implementation("org.springframework.boot:spring-boot-starter-flyway")

  implementation("org.flywaydb:flyway-database-oracle")
  implementation("org.hibernate.orm:hibernate-community-dialects:7.2.3.Final")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

  runtimeOnly("com.zaxxer:HikariCP")
  runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.26.0.0.0")
  implementation("com.oracle.database.messaging:aqapi-jakarta:23.8.0.0")

  implementation("org.apache.commons:commons-lang3:3.20.0")
  // Needs to match this version https://github.com/microsoft/ApplicationInsights-Java/blob/<version>/dependencyManagement/build.gradle.kts#L16
  // where <version> is the version of application insights pulled in by hmpps-gradle-spring-boot
  // at https://github.com/ministryofjustice/hmpps-gradle-spring-boot/blob/main/src/main/kotlin/uk/gov/justice/digital/hmpps/gradle/configmanagers/AppInsightsConfigManager.kt#L7
  implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.21.0")
  implementation("org.jetbrains.exposed:exposed-spring-boot4-starter:1.0.0")
  implementation("org.jetbrains.exposed:exposed-java-time:1.0.0")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.0.0")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.awaitility:awaitility-kotlin:4.3.0")

  testImplementation("org.mockito:mockito-inline:5.2.0")

  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.37") {
    exclude(group = "io.swagger.core.v3")
  }
  testImplementation("io.swagger.core.v3:swagger-core-jakarta:2.2.41")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:localstack:1.21.4")
  testImplementation("org.testcontainers:oracle-xe:1.21.4")
}

kotlin {
  jvmToolchain(25)
  compilerOptions {
    freeCompilerArgs.addAll("-Xwhen-guards", "-Xannotation-default-target=param-property")
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }
  test {
    jvmArgs("-Doracle.jakarta.jms.useEmulatedXA=false")
  }
}
