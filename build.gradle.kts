plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    `java-library`
    `maven-publish`
}

group = "com.patryklikus"
version = "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

extra["springBootVersion"] = "4.0.3"

dependencies {
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}"))

    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-tx")
    compileOnly("org.springframework.session:spring-session-core")
    compileOnly("org.springframework.data:spring-data-commons")
    compileOnly("org.springframework.data:spring-data-jpa")
    compileOnly("jakarta.persistence:jakarta.persistence-api")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}"))
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.session:spring-session-core")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "spring-session-jpa"
                description = "JPA-backed Spring Session with FK support and full entity customization"
                url = "https://github.com/liqs02/spring-session-jpa"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/liqs02/spring-session-jpa")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
