package io.github.huskcasaca.gradlecurseforgeplugin.tasks

import io.github.huskcasaca.gradlecurseforgeplugin.ChangelogType
import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifactRepository
import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgePublication
import io.github.huskcasaca.gradlecurseforgeplugin.ReleaseType
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.AbstractCurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.repositories.DefaultCurseForgeArtifactRepository
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersion
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersionType
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.UploadMetadata
import io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.CurseForgePublicationInternal
import io.github.huskcasaca.gradlecurseforgeplugin.internal.utils.finalizeAndGet
import io.github.huskcasaca.gradlecurseforgeplugin.internal.utils.finalizeAndGetOrNull
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "Not made cacheable, yet")
public open class GeneratePublicationMetadata @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:Internal
    public val publication: Property<CurseForgePublication> = objects.property()

    private val apiKey: Property<String> = project.objects.property()

    private var _repository: CurseForgeArtifactRepository? = null

    @get:Internal
    public var repository: CurseForgeArtifactRepository?
        get() = _repository
        set(value) {
            _repository = value!!
            apiKey.set(value.token)
        }

    private val url: String
        get() = (repository as DefaultCurseForgeArtifactRepository).url.get()


    @TaskAction
    public fun generate(): Unit = runBlocking {
        val publication = publication.finalizeAndGet() as CurseForgePublicationInternal

        val json = Json {
            prettyPrint = true
        }

        generateArtifactMetadata(json, publication.artifact)
        publication.extraArtifacts.forEach { artifact -> generateArtifactMetadata(json, artifact) }
    }

    private suspend fun generateArtifactMetadata(json: Json, artifact: AbstractCurseForgeArtifact) {
        val apiKey = apiKey.finalizeAndGetOrNull() ?: error("CurseForge API key has not been provided for repository: ${_repository!!.name}")

        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val gameVersionTypes = httpClient.resolveGameVersionTypes(apiKey)
        val gameVersions = httpClient.resolveGameVersions(apiKey)

        val gameVersionIds = gameVersionTypes.flatMap { type ->
            gameVersions.filter { version ->
                version.gameVersionTypeID == type.id
            }.mapNotNull { version ->
                version.id.takeIf { artifact.isGameVersionIncluded(type, version) }
            }
        }

        val metadata = UploadMetadata(
            displayName = artifact.displayNameSafe ?: artifact.file.name,
            releaseType = artifact.releaseType.toJSONType(),
            changelogType = artifact.changelog.type.toJSONType(),
            changelog = artifact.changelog.content,
            gameVersions = gameVersionIds
        )

        File("${artifact.file.absolutePath}.metadata.json").apply {
            parentFile.mkdir()
            writeText(json.encodeToString(metadata))
        }
    }

    private fun ChangelogType.toJSONType(): String = when (this) {
        ChangelogType.HTML -> "html"
        ChangelogType.MARKDOWN -> "markdown"
        ChangelogType.TEXT -> "text"
    }

    private fun ReleaseType.toJSONType(): String = when (this) {
        ReleaseType.ALPHA -> "alpha"
        ReleaseType.BETA -> "beta"
        ReleaseType.RELEASE -> "release"
    }

    private suspend fun HttpClient.resolveGameVersionTypes(apiKey: String): List<GameVersionType> =
        get {
            url("${this@GeneratePublicationMetadata.url}/api/game/version-types")
            header("X-Api-Token", apiKey)
        }.body()

    private suspend fun HttpClient.resolveGameVersions(apiKey: String): List<GameVersion> =
        get {
            url("${this@GeneratePublicationMetadata.url}/api/game/versions")
            header("X-Api-Token", apiKey)
        }.body()

}