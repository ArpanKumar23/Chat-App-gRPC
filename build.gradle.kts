import com.google.protobuf.gradle.id

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.2.21"
	id("com.google.protobuf") version "0.9.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// 1. The BOM: This forces ALL gRPC dependencies in the project to use 1.62.2
	implementation(enforcedPlatform("io.grpc:grpc-bom:1.62.2"))

	// 2. The Spring Boot Starter (it will now be forced to use the 1.62.2 core from the BOM)
	implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

	// 3. Your gRPC libraries (Notice the versions are removed so they inherit 1.62.2)
	implementation("io.grpc:grpc-protobuf")
	implementation("io.grpc:grpc-stub")
	implementation("io.grpc:grpc-netty-shaded") // Added this! The client needs it for the network channel

	// Kotlin stub maintains its own separate versioning from core gRPC
	implementation("io.grpc:grpc-kotlin-stub:1.4.1")

	// Kotlin Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

	// --- Your existing Spring, DB, and Kotlin dependencies ---
	implementation("org.postgresql:postgresql:42.7.3")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	runtimeOnly("org.postgresql:postgresql")

	// Test dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("com.example.chat_application.ChatApplicationKt")
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:3.25.3"
	}

	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.62.2"
		}
		id("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
		}
	}

	generateProtoTasks {
		all().forEach {
			it.plugins {
				id("grpc")
				id("grpckt")
			}
		}
	}
}

tasks.register<JavaExec>("runClient") {
	group = "application"
	description = "Runs the Chat CLI Client"

	mainClass.set("com.example.chat_application.client.ChatClientKt")

	classpath = sourceSets["main"].runtimeClasspath

	standardInput = System.`in`
}