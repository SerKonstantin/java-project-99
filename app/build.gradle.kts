import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	application
	checkstyle
	jacoco
	id("org.springframework.boot") version "3.3.0-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.4"
	id("com.github.ben-manes.versions") version "0.50.0"
	id("io.freefair.lombok") version "8.4"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application {
	mainClass.set("hexlet.code.app.AppApplication")
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.openapitools:jackson-databind-nullable:0.2.6")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	runtimeOnly("com.h2database:h2:2.2.224")
	runtimeOnly("org.postgresql:postgresql:42.7.3")

	implementation("org.instancio:instancio-junit:4.5.0")
	implementation("net.datafaker:datafaker:2.1.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation(platform("org.junit:junit-bom:5.10.2"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.7")
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
		events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
		showStandardStreams = true
	}
}

jacoco {
	toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test) // tests are required to run before generating the report
	reports {
		xml.required = true
	}
}
