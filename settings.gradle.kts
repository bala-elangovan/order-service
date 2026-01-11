pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    // Map custom plugin IDs to JitPack coordinates
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "io.github.balaelangovan") {
                useModule("com.github.bala-elangovan.gradle-plugins:spring-conventions:${requested.version}")
            }
        }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
    }
}

rootProject.name = "order-service"

// Multi-module structure with orders parent module
include("orders")
include("orders:orders-domain")
include("orders:orders-infra")
include("orders:orders-app")
include("orders:orders-consumer")
