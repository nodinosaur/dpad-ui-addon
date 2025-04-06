plugins {
    id("java")

    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm
    id("org.jetbrains.kotlin.jvm") version "2.1.20"

    // https://plugins.gradle.org/plugin/org.jetbrains.intellij.platform
    // https://github.com/JetBrains/gradle-intellij-plugin/releases
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "uk.co.androidalliance"
version = "1.0.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = true
    projectName = project.name
    autoReload = true

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
            untilBuild = "251.*"
        }
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}

dependencies {

    intellijPlatform {
        instrumentationTools()
        androidStudio("2024.3.1.14")
        bundledPlugins(
            "org.jetbrains.kotlin",
            "org.jetbrains.android",
            "com.intellij.java",
        )
    }



}
