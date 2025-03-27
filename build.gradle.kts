import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        instrumentationTools()

        val ideaVersion = properties("ideaVersion")
        val pycharmVersion = properties("pycharmVersion")


        if (project.hasProperty("isPyCharm")) {
            logger.quiet("PyCharm build enabled")
            create(IntelliJPlatformType.PyCharmCommunity, pycharmVersion)
        } else if (project.hasProperty("isIDEA")) {
            logger.quiet("IDEA build enabled")
            create(IntelliJPlatformType.IntellijIdeaCommunity, ideaVersion)
        } else {
            logger.quiet("Default IDEA build enabled")
            create(IntelliJPlatformType.IntellijIdeaCommunity, ideaVersion)
        }
        instrumentationTools()
        pluginVerifier()
        // zipSigner()
        // testFramework(TestFrameworkType.Platform)

    }
    api(libs.okhttp)
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.jsoup)
    implementation(libs.slf4j)
    implementation(libs.testit.api)
    testImplementation(kotlin("test"))
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")

        description = "The Test IT Management plugin is a powerful tool for managing test cases. It provides an ability to browse work items hierarchies, generate unit tests for selected scenarios."
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
        vendor {
            name = "Test IT"
            url = "https://testit.software"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}


kotlin {
    jvmToolchain(properties("javaVersion").get().toInt())
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
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
        val logPropsFile = sourceSets.main.get().resources.find {
            it.name.endsWith("logging.properties")
        } as File

        if (logPropsFile.exists()) {
            systemProperty("java.util.logging.config.file", logPropsFile)
        }

        systemProperty(
            "java.util.logging.manager",
            "java.util.logging.LogManager"
        )
        systemProperty(
            "TEST_CI",
            System.getProperty("testCi")
        )
        reports {
            html.required.set(true)
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


