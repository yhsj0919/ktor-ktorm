import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val ktormVersion: String by project

val mysqlVersion: String by project
val koinVersion: String by project
val hikariVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    //https://imperceptiblethoughts.com/shadow/
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "xyz.yhsj.ktor"
version = "1.0.5"
application {
    mainClass.set("xyz.yhsj.ktor.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}




repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    //https://github.com/ktorio/ktor
    //Ktor2.0用到的依赖，koin3.1.3无法兼容
    implementation("io.ktor:ktor-server-core:$ktor_version")
//    implementation("io.ktor:ktor-server-sessions:$ktor_version")
//    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
//    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-auto-head-response:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
//
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    //校验
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.glassfish:jakarta.el:4.0.2")
    // 依赖注入
    //https://github.com/InsertKoinIO/koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    //mongodb数据库
//    //https://github.com/Litote/kmongo
//    implementation("org.litote.kmongo:kmongo:4.7.1")
//    implementation("org.litote.kmongo:kmongo-coroutine:4.7.1")
    //Redis
    //https://github.com/redis/jedis
    implementation("redis.clients:jedis:4.2.3")

    //excel解析
    //https://github.com/alibaba/easyexcel
    implementation("com.alibaba:easyexcel:3.1.1")
    implementation(kotlin("stdlib-jdk8"))


    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-jackson:$ktormVersion")
    implementation("org.ktorm:ktorm-support-mysql:$ktormVersion")
    implementation("mysql:mysql-connector-java:$mysqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("io.ktor:ktor-server-auth-jvm:2.1.2")
    implementation("io.ktor:ktor-server-core-jvm:2.1.2")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.1.2")

}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}