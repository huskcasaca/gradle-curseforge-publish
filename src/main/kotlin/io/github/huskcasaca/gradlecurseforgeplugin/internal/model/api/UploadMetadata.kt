package io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api

import kotlinx.serialization.*

@Serializable
internal data class UploadMetadata(
    @SerialName("displayName") val displayName: String?,
    @SerialName("releaseType") val releaseType: String,
    @SerialName("changelogType") val changelogType: String,
    @SerialName("changelog") val changelog: String,
    @SerialName("parentFileID") val parentFileId: Int? = null,
    @SerialName("gameVersions") val gameVersions: List<Int>?,
    // TODO relations
)