import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
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
