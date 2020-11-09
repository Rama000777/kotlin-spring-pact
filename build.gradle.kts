import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.wiredforcode.gradle.spawn.*

plugins {
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("au.com.dius.pact") version "4.1.0"
    id("com.wiredforcode.spawn") version "0.8.2"
    id("com.sourcemuse.mongo") version "1.0.7"
}


group = "com.sample"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("org.apache.commons:commons-collections4:4.4")
    compileOnly("org.projectlombok:lombok")
    compileOnly("com.vdurmont:emoji-java:5.1.1")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("au.com.dius:pact-jvm-provider-junit5:4.0.10")
    testImplementation("au.com.dius:pact-jvm-consumer-junit5:4.0.10")
    testImplementation("ru.lanwen.wiremock:wiremock-junit5:1.3.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.suppressWarnings = false
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
        kotlinOptions.suppressWarnings = false
    }
}

tasks.withType<SpawnProcessTask>{
    command = "java -Dspring.profiles.active=test -jar ${projectDir}/build/libs/kotlindemos-0.0.1-SNAPSHOT.jar"
    ready = "Started MainApplication"
}

tasks.withType<KillProcessTask> {

}

pact {
    publish {
        pactBrokerUrl = "http://localhost:9292"
    }

    serviceProviders {
        
        broker {
            pactBrokerUrl = "http://localhost:9292"
        }
        reports {
            reports.put("console", "console")
            reports.put("json", "json")
            reports.put("markdown", "markdown")
        }
        publish {
            pactBrokerUrl = "http://localhost:9292"
        }
    }
}
