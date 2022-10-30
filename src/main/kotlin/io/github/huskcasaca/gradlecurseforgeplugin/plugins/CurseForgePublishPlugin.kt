package io.github.huskcasaca.gradlecurseforgeplugin.plugins

import io.github.huskcasaca.gradlecurseforgeplugin.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.utils.*
import io.github.huskcasaca.gradlecurseforgeplugin.tasks.*
import org.apache.log4j.LogManager
import org.gradle.api.*
import org.gradle.api.internal.file.*
import org.gradle.api.invocation.*
import org.gradle.api.model.*
import org.gradle.api.plugins.*
import org.gradle.api.publish.*
import org.gradle.api.publish.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import java.util.*
import javax.inject.*

public class CurseForgePublishPlugin @Inject constructor(
    private val objectFactory: ObjectFactory,
    private val fileResolver: FileResolver
) : Plugin<Project> {

    internal companion object {

        private val LOGGER = LogManager.getLogger(CurseForgePublishPlugin::class.java)

        lateinit var gradle: Gradle
            private set

    }

    override fun apply(target: Project): Unit = applyTo(target) {
        pluginManager.apply(PublishingPlugin::class)

        CurseForgePublishPlugin.gradle = gradle

        extensions.configure<PublishingExtension> {
            repositories {
                this as ExtensionAware

                extensions.create("curseForge", CurseForgeRepositoryExtension::class, repositories)
            }

            publications.registerFactory(CurseForgePublication::class.java, CurseForgePublicationFactory(fileResolver))
            realizePublishingTasksLater(target)
        }

        configureForgeGradleIntegration()
        configureJavaIntegration()
    }

    private fun PublishingExtension.realizePublishingTasksLater(project: Project) {
        val curseForgePublications = publications.withType<CurseForgePublicationInternal>()
        val tasks = project.tasks

        val publishLifecycleTask = tasks.named(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
        val repositories = repositories.withType<CurseForgeArtifactRepository>()

        repositories.all repository@{
            tasks.register(publishAllToSingleRepoTaskName(this@repository)) {
                description = "Publishes all CurseForge publications produced by this project to the ${this@repository.name} repository."
                group = PublishingPlugin.PUBLISH_TASK_GROUP
            }
        }

        curseForgePublications.all publication@{
            repositories.all repository@{
                createGenerateMetadataTask(tasks, this@publication, this@repository)
                createPublishTask(tasks, publishLifecycleTask, this@publication, this@repository)
            }
        }
    }

    private fun publishAllToSingleRepoTaskName(repository: CurseForgeArtifactRepository): String =
        "publishAllPublicationsTo${repository.name.get().capitalize(Locale.ROOT)}Repository"

    private fun createGenerateMetadataTask(
        tasks: TaskContainer,
        publication: CurseForgePublicationInternal,
        repository: CurseForgeArtifactRepository
    ) {
        val generatorTaskName = "generateMetadataFilesFor${publication.name.capitalize(Locale.ROOT)}Publication"

        tasks.register<GeneratePublicationMetadata>(generatorTaskName) {
            description = "Generates CurseForge metadata for publication '${publication.name}'."
            group = PublishingPlugin.PUBLISH_TASK_GROUP

            this.repository = repository
            this.publication.set(publication)
        }
    }

    private fun createPublishTask(
        tasks: TaskContainer,
        publishLifecycleTask: TaskProvider<Task>,
        publication: CurseForgePublicationInternal,
        repository: CurseForgeArtifactRepository
    ) {

        val generatorTaskName = "generateMetadataFilesFor${publication.name.capitalize(Locale.ROOT)}Publication"
        val publishTaskName = "publish${publication.name.capitalize(Locale.ROOT)}PublicationTo${repository.name.get().capitalize(Locale.ROOT)}Repository"

        tasks.register<PublishToCurseForgeRepository>(publishTaskName) {
            dependsOn(generatorTaskName)

            description = "Publishes CurseForge publication '${publication.name}' to CurseForge repository '${repository.name}'"
            group = PublishingPlugin.PUBLISH_TASK_GROUP
            this.repository = repository
            this.publication = publication
        }

        publishLifecycleTask.configure { dependsOn(publishTaskName) }
        tasks.named(publishAllToSingleRepoTaskName(repository)) { dependsOn(publishTaskName) }

    }

    private fun Project.configureForgeGradleIntegration() {
        pluginManager.withPlugin("net.minecraftforge.gradle") {
            extensions.configure<PublishingExtension> {
                publications.withType<CurseForgePublication> {
                    loaders.add(LoaderType.FORGE)

                    afterEvaluate {
                        val mcVersion = this@configureForgeGradleIntegration.extensions.extraProperties["MC_VERSION"] as String
                        val matchGroups = """^([0-9]+)\.([0-9]+)(?:\.([0-9]+))?""".toRegex().matchEntire(mcVersion)?.groupValues

                        if (matchGroups == null) {
                            LOGGER.warn("Failed to parse Minecraft version string '$mcVersion'. The CurseForge publication cannot infer the required Minecraft version.")
                            return@afterEvaluate
                        }

                        val mcDependencySlug = "minecraft-${matchGroups[1]}-${matchGroups[2]}"
                        val mcVersionSlug = "${matchGroups[1]}-${matchGroups[2]}-${matchGroups[3]}"

                        LOGGER.debug("Inferred CurseForge Minecraft dependency: type='$mcDependencySlug', version='$mcVersionSlug'")
                        // TODO:
//                        includeGameVersions { type, version -> type == mcDependencySlug && version == mcVersionSlug }
                    }
                }
            }

            afterEvaluate {
                tasks.withType<AbstractPublishToCurseForge> {
                    dependsOn(tasks["build"])
                }
            }
        }
    }

    private fun Project.configureJavaIntegration() {
        pluginManager.withPlugin("java") {
            extensions.configure<PublishingExtension> {
                publications.withType<CurseForgePublication>().configureEach {

                    val jar = tasks.named(JavaPlugin.JAR_TASK_NAME)

                    println("================================ task name " + JavaPlugin.JAR_TASK_NAME)

                    artifact(jar)

                    val compileJava = tasks.named<JavaCompile>(JavaPlugin.COMPILE_JAVA_TASK_NAME).get()
                    val targetVersionProvider = compileJava.options.release.map(Int::toString)
                        .orElse(compileJava.targetCompatibility)
                        .map {
                            // Normalize (e.g 1.8 => 8)
                            val majorVersion = JavaVersion.toVersion(it).majorVersion
                            LOGGER.debug("Inferred CurseForge Java dependency: version='java-$majorVersion'")
                            majorVersion
                        }

                    javaVersion(targetVersionProvider)
                }
            }
        }
    }

    private inner class CurseForgePublicationFactory(
        private val fileResolver: FileResolver
    ) : NamedDomainObjectFactory<CurseForgePublication> {

        override fun create(name: String): CurseForgePublication {
            return objectFactory.newInstance<DefaultCurseForgePublication>(name, CurseForgeArtifactNotationParser(fileResolver))
        }

    }

}