plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.conventions.web)
}

dependencies {
    // Implementation
    implementation(project(":orders:orders-domain"))
    implementation(project(":orders:orders-infra"))
    implementation(libs.spring.webmvc.starter)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.kafka)
    implementation(libs.bundles.coroutines)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.spring.boot.starter.flyway)

    // Runtime only
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.postgresql)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.kafka.test)
}
