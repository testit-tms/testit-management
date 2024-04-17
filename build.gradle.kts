import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.changelog)
    alias(libs.plugins.intellij)
    alias(libs.plugins.kotlin)
}

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(libs.okhttp)
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.jsoup)
    implementation(libs.slf4j)
    implementation(libs.testit.api)
    testImplementation(kotlin("test"))
    testCompileOnly(libs.testit.common)
}

changelog {
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(setOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    lineSeparator.set(System.lineSeparator())
    combinePreReleases.set(true)
    repositoryUrl = properties("pluginRepositoryUrl")
}

intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")
    plugins = properties("platformPlugins").map {
        it.split(',').map(String::trim).filter(String::isNotEmpty)
    }
    downloadSources = true
}

kotlin {
    jvmToolchain(properties("javaVersion").get().toInt())
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                val lineSeparator = System.lineSeparator()

                if (!containsAll(setOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:$lineSeparator$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString(lineSeparator).let(::markdownToHTML)
            }
        }

        val changelog = project.changelog

        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token = environment("PUBLISH_TOKEN")
        channels = properties("pluginVersion").map {
            listOf(
                it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }
            )
        }
    }

    signPlugin {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    withType<JavaCompile>().configureEach {
        sourceCompatibility = properties("javaVersion").get()
        targetCompatibility = properties("javaVersion").get()
        options.setIncremental(true)
        options.isFork = true
        options.encoding = properties("javaEncoding").get()
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            apiVersion = KotlinVersion.KOTLIN_1_7
            languageVersion = KotlinVersion.KOTLIN_1_9
            incremental = true
        }
    }

    withType<Test>().configureEach {
        systemProperty(
            "java.util.logging.config.file",
            sourceSets.main.get().resources.find { it.name.endsWith("logging.properties") } as File
        )
        systemProperty(
            "java.util.logging.manager",
            "java.util.logging.LogManager"
        )
        reports {
            html.required.set(false)
            junitXml.required.set(false)
        }
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            showCauses = false
            showStackTraces = false
            showStandardStreams = true
        }
        outputs.upToDateWhen { false }
        useJUnitPlatform()
    }
}
