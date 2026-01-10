buildscript {
    repositories {
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.bala-elangovan.gradle-plugins:spring-conventions:v0.1.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
}

apply(plugin = "io.github.balaelangovan.spring-core-conventions")

// orders-domain is a library, not an application - disable bootJar
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    // Implementation
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.slf4j.api)
    implementation(libs.jackson.module.kotlin)

    // Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
