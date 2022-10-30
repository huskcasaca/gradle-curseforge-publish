package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

class CurseForgePublishPluginTestKitTest extends Specification {

    private static def GRADLE_VERSIONS = [
        "7.4",
        "7.4.1",
        "7.4.2",
        "7.5",
        "7.5.1"
    ]

    @TempDir
    File projectDir
    File buildFile
    File settingsFile

    def setup() {
        buildFile = new File(projectDir, "build.gradle")
        settingsFile = new File(projectDir, "settings.gradle")
    }

    @Unroll
    def "configure (Gradle #gradleVersion)"() {
        given:
        buildFile << """\
            import io.github.huskcasaca.gradlecurseforgeplugin.*
            
            plugins {
                id 'java'
                id 'io.github.huskcasaca.gradle-curseforge-plugin'
            }
            
            java {
                toolchain {
                    languageVersion = JavaLanguageVersion.of(8)
                }
            }
            
            tasks.all {
                enabled = false
            }
            
            publishing {
                repositories {
                    curseForge {
                        repository {
                            /*
                             * In a real application, it is recommended to store the API key outside the build script.
                             *
                             * // Store the key in "~/.gradle/gradle.properties"
                             * apiKey = extra["cfApiKey"]
                             */
                            apiKey = "123e4567-e89b-12d3-a456-426614174000"
                        }
                    }
                }
                publications {
                    curseForge(CurseForgePublication) {
                        projectID = 123456 // The CurseForge project ID (required)
            
                        // Specify which game and version the mod/plugin targets (optional)
                        // When using the ForgeGradle plugin, this information is usually inferred and set automatically.
                        includeGameVersions { type, version -> type == "minecraft-1-16" && version == "1-16-5" }
            
                        artifact {
                            changelog = new Changelog("Example changelog...", ChangelogType.TEXT) // The changelog (required)
                            releaseType = ReleaseType.RELEASE // The release type (required)
            
                            displayName = "Example Project" // A user-friendly name for the project (optional)
                        }
                    }
                }
            }
        """.stripIndent()

        when:
        def result = runGradle(gradleVersion, "publish")

        then:
        result.task(":publish").outcome == TaskOutcome.SKIPPED

        where:
        gradleVersion << GRADLE_VERSIONS
    }

    private runGradle(String version, String... args) {
        def arguments = []
        arguments.addAll(args)
        arguments.add("-s")

        GradleRunner.create()
                .withGradleVersion(version)
                .withProjectDir(projectDir)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

}