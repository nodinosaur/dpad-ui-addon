plugins {
    id("java")

    //kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.0.0"
}

group = "uk.co.androidalliance"
version = "1.0-SNAPSHOT"

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
            untilBuild = "243.*"
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
