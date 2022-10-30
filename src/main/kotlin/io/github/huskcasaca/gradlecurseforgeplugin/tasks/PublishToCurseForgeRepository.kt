package io.github.huskcasaca.gradlecurseforgeplugin.tasks

import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifactRepository
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.AbstractCurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.repositories.DefaultCurseForgeArtifactRepository
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.UploadMetadata
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.UploadResponse
import io.github.huskcasaca.gradlecurseforgeplugin.internal.utils.finalizeAndGet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Not worth caching")
public open class PublishToCurseForgeRepository : AbstractPublishToCurseForge() {

    private val apiKey: Property<String> = project.objects.property()

    private var _repository: CurseForgeArtifactRepository? = null

    private val json = Json {
        ignoreUnknownKeys = false
        explicitNulls = false
    }

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
    public fun publish(): Unit = runBlocking {
        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        val publication = publicationInternal!!

        val id = publication.id.finalizeAndGet()

        val parentFileId = publication.artifact.let { artifact ->
            httpClient.doUploadFile(artifact, id = id, parentFileId = null)
        }

        publication.extraArtifacts.forEach { artifact ->
            httpClient.doUploadFile(artifact, id = id, parentFileId = parentFileId)
        }
    }

    private suspend fun HttpClient.doUploadFile(
        artifact: AbstractCurseForgeArtifact,
        id: Int,
        parentFileId: Int? = null,
    ): Int {
        val metadata = json.decodeFromString<UploadMetadata>(File("${artifact.file.absolutePath}.metadata.json").readText()).copy(parentFileId = parentFileId)

        require((parentFileId != null) xor (metadata.gameVersions != null)) { "Exactly one of the parameter must be set: parentFile, gameVersions" }

        val httpResponse = submitFormWithBinaryData(
            url = "${this@PublishToCurseForgeRepository.url}/api/projects/${id}/upload-file",
            formData = formData {
                append(
                    "metadata",
                    json.encodeToString(metadata),
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
                append(
                    "file",
                    artifact.file.readBytes(),
                    headersOf(HttpHeaders.ContentDisposition, "filename=${artifact.file.name}")
                )
            }
        ) {
            header("X-Api-Token", apiKey.get())
        }

        if (httpResponse.status.isSuccess()) {
            return httpResponse.body<UploadResponse>().id
        } else if (httpResponse.contentType()?.match(ContentType.Application.Json) == true) {
            error(httpResponse.body<String>())
        } else {
            error("Publishing CurseForge publication '${publication!!.name}' to ${repository!!.name} failed with status code: ${httpResponse.status}")
        }
    }

}