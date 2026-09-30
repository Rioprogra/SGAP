// settings.gradle.kts: dice a Gradle de dónde descargar plugins/librerías
// y qué módulos forman el proyecto (aquí solo ":app").
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SGAP"
include(":app")
