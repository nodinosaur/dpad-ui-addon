plugins {
    id("java")

    // Must match the Kotlin version bundled with the IDE
    // https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#kotlin-standard-library
    // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
    // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm
    id("org.jetbrains.kotlin.jvm") version "2.1.20"

    // https://plugins.gradle.org/plugin/org.jetbrains.intellij.platform
    // https://github.com/JetBrains/gradle-intellij-plugin/releases
    id("org.jetbrains.intellij.platform") version "2.5.0"

    id ("org.jetbrains.changelog") version "2.2.1"
}
group = project.providers.gradleProperty("pluginGroup").get()
version = project.providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

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
        group = providers.gradleProperty("pluginGroup")
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChainFile = file("./keystore/chain.crt")
        privateKeyFile = file("./keystore/private.pem")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}

dependencies {

    intellijPlatform {
        bundledPlugins(
            "org.jetbrains.kotlin",
            "org.jetbrains.android",
            "com.intellij.java",
        )
        //androidStudio("2024.3.1.14")
        if (project.hasProperty("localIdeOverride")) {
            local(property("localIdeOverride").toString())
        } else {
            androidStudio(property("ideVersion").toString())
        }
    }


}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

