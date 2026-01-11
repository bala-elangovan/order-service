pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
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
