import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogSectionUrlBuilder
import org.jetbrains.changelog.date

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

dependencies {
    intellijPlatform {
        bundledPlugins(
            "org.jetbrains.kotlin",
            "org.jetbrains.android",
            "com.intellij.java",
        )
        if (project.hasProperty("localIdeOverride")) {
            local(property("localIdeOverride").toString())
        } else {
            androidStudio(property("ideVersion").toString())
        }
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

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

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

changelog {
    header.set(provider { "[${version.get()}] - ${date()}" })
    version.set(project.version.toString())
    groups.empty()
    keepUnreleasedSection.set(false)
    itemPrefix.set("-")
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

