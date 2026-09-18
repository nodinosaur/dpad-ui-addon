import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import java.util.*

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

    id("org.jetbrains.changelog") version "2.2.1"

    id("com.google.protobuf") version "0.9.4"
}
group = project.providers.gradleProperty("pluginGroup").get()
version = project.providers.gradleProperty("pluginVersion").get()

// Keep these in lockstep. The generated gRPC stubs call runtime APIs that only exist in a
// matching (or newer) grpc-java — 1.84's gencode uses `ClientCalls.blockingV2UnaryCall`,
// which 1.68 does not have. grpc-protobuf 1.84 still targets the protobuf 3.25.x line.
val grpcVersion = "1.84.0"
val grpcKotlinVersion = "1.4.1"
val protobufVersion = "3.25.9"

// A locally installed IDE, if `localIdePath` points at one. Used for two things: an extra
// `verifyPlugin` target, and the `runLocalIde` task. Null when unset or missing, so nothing
// here depends on a machine-specific path.
val localIdeFile = providers.gradleProperty("localIdePath")
    .map(String::trim)
    .filter(String::isNotEmpty)
    .map(::file)
    .orNull
    ?.takeIf { it.exists() }

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

    // gRPC + Protobuf
    // Exclude Guava — IntelliJ Platform bundles its own; shipping a second copy causes
    // classloader constraint violations for ListenableFuture at runtime.
    implementation("io.grpc:grpc-protobuf:$grpcVersion") {
        exclude(group = "com.google.guava")
    }
    implementation("io.grpc:grpc-stub:$grpcVersion") {
        exclude(group = "com.google.guava")
    }
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion") {
        exclude(group = "com.google.guava")
    }
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion") {
        exclude(group = "com.google.guava")
    }

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
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
            // Open-ended on purpose: the plugin stays installable on future IDE releases
            // without a re-release. `provider { null }` is how the IntelliJ Platform Gradle
            // plugin is told to omit `until-build` entirely.
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChainFile = file("./keystore/chain.crt")
        privateKeyFile = file("./keystore/private.pem")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.provider {
            val securePropsFile = rootProject.file("./keystore/publish.properties")
            var foundToken: String? = null

            if (securePropsFile.exists() && securePropsFile.isFile) {
                try {
                    val properties = Properties()
                    securePropsFile.inputStream().use { properties.load(it) }
                    foundToken = properties.getProperty("publish.token")
                    if (!foundToken.isNullOrBlank()) {
                        project.logger.lifecycle("Using publish token from ${securePropsFile.name}")
                    } else {
                        foundToken = null
                    }
                } catch (e: Exception) {
                    project.logger.warn("Could not read publish token from ${securePropsFile.absolutePath}: ${e.message}")
                }
            }

            if (foundToken == null) {
                foundToken = providers.environmentVariable("PUBLISH_TOKEN").orNull
                if (!foundToken.isNullOrBlank()) {
                    project.logger.lifecycle("Using publish token from environment variable PUBLISH_TOKEN")
                } else {
                    foundToken = null
                }
            }

            // If still not found after checking both, fail the build configuration
            // Or return null if the token is optional for some tasks (publishing task will likely fail later if null)
            foundToken ?: error(
                """
                    Publish token not found in ${securePropsFile.name} (key 'publish.token') or environment variable 'PUBLISH_TOKEN'. 
                    See build script for details.
                """.trimIndent()
            )

        } // End provider lambda
    } // End publishing block

    pluginVerification {
        ides {
            // NOT `recommended()`, and not `select { untilBuild = ... }`.
            //
            // `recommended()` follows the plugin's compatibility range, which is now
            // open-ended, so it walks forward onto Android Studio RC/Canary builds. Those
            // are published as .dmg installers rather than resolvable Gradle artifacts —
            // e.g. 2026.2.1.6 "rabbit1-rc1", which is what broke `verifyPlugin`.
            //
            // A `select { }` filter does not help either: its `untilBuild` is ignored here
            // (verified with `printProductsReleases`), because the bound is inherited from
            // `patchPluginXml`, where `until-build` is deliberately absent.
            //
            // So pin the targets. Verifying the oldest supported build and the build we
            // compile against is what actually catches API breakage, and keeps the download
            // to two IDEs instead of six.
            ide(IntelliJPlatformType.AndroidStudio, "2024.3.2.15")
            ide(IntelliJPlatformType.AndroidStudio, providers.gradleProperty("ideVersion").get())

            // Additionally verify against a locally installed IDE, when one is present.
            // The compatibility range is open-ended, so it is worth checking a build newer
            // than `ideVersion` — and a local install costs nothing to resolve. Silently
            // skipped when the path does not exist, so CI and other machines are unaffected.
            localIdeFile?.let { local(it) }
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            // grpc-java publishes an x86_64 Mach-O binary under the `osx-aarch_64`
            // classifier (verified for 1.68 through 1.75), so on Apple Silicon protoc fails
            // with "bad CPU type in executable" unless Rosetta is installed. Setting
            // `protocGenGrpcJavaPath` points at a native build instead — e.g.
            // `brew install protoc-gen-grpc-java`. Unset, the Maven artifact is used.
            // Whichever is used must match `grpcVersion` — a newer generator emits calls
            // against runtime APIs an older grpc-java does not have.
            val nativePath = providers.gradleProperty("protocGenGrpcJavaPath")
                .map(String::trim)
                .filter(String::isNotEmpty)
                .orNull

            if (nativePath != null) {
                path = nativePath
            } else {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
        }
        create("grpckt") {
            // Pure-JVM jar, so no architecture problem here.
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Registers `runLocalIde`, which launches the locally installed IDE with the plugin, rather
// than the sandboxed `ideVersion` that `runIde` downloads. Only registered when
// `localIdePath` resolves, so the build stays portable.
localIdeFile?.let { ide ->
    intellijPlatformTesting.runIde.register("runLocalIde") {
        localPath = ide
    }
}

