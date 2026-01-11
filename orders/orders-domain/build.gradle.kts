plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.conventions.core)
}

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

    // Test - Kotest and MockK provided by spring-conventions plugin
    testImplementation(libs.kotlinx.coroutines.test)
}
