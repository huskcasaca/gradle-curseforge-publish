import org.jetbrains.kotlin.gradle.tasks.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    groovy
    `java-test-fixtures`
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlin.platform.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.plugin.publish)
}

group = "io.github.huskcasaca.gradlecurseforgeplugin"
version = "1.0.0-alpha"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
}

gradlePlugin {
    plugins {
        create("curseForgePublish") {
            id = "io.github.huskcasaca.gradle-curseforge-plugin"
            displayName = "CurseForge Gradle Publish Plugin"
            description = "A Gradle plugin for publishing to CurseForge"
            implementationClass = "io.github.huskcasaca.gradlecurseforgeplugin.plugins.CurseForgePublishPlugin"
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.serialization)
    implementation("io.ktor:ktor-client-content-negotiation:2.1.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.1.3")

    testFixturesApi(platform(libs.spock.bom))
    testFixturesApi(libs.spock.core)
}