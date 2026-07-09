val ktorVersion = providers.gradleProperty("ktorVersion").get()
val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
val logbackVersion = providers.gradleProperty("logbackVersion").get()

plugins {
    application
    kotlin("jvm") version "2.4.0"
    id("com.gradleup.shadow") version "9.5.1"
}

group = "xyz.yhsj.ktor"
version = "1.1.3"

application {
    mainClass.set("xyz.yhsj.ktor.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-thymeleaf:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    implementation("org.hibernate.validator:hibernate-validator:9.1.2.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")

    implementation("io.insert-koin:koin-core:4.2.2")
    implementation("io.insert-koin:koin-ktor:4.2.2")
    implementation("io.insert-koin:koin-logger-slf4j:4.2.2")

    implementation("redis.clients:jedis:7.5.3")
    implementation("com.alibaba:easyexcel:4.0.3")

    implementation("org.ktorm:ktorm-core:4.2.1")
    implementation("org.ktorm:ktorm-jackson:4.2.1")
    implementation("org.ktorm:ktorm-support-mysql:4.2.1")
    implementation("com.mysql:mysql-connector-j:9.7.0")
    implementation("com.zaxxer:HikariCP:7.1.0")

    implementation("com.belerweb:pinyin4j:2.5.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
