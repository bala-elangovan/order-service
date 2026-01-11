plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.conventions.core)
}

// orders-infra is a library, not an application - disable bootJar
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    // Implementation
    implementation(project(":orders:orders-domain"))
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.kotlin.reflect)
    implementation(libs.bundles.coroutines)
    implementation(libs.jackson.module.kotlin)

    // Runtime only
    runtimeOnly(libs.postgresql)

    // Test
    testImplementation(libs.spring.boot.starter.test)
}
