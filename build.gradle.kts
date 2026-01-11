// Multi-module root project

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
}

group = "io.github.balaelangovan"
version = "${property("major")}.${property("minor")}.${property("patch")}"
