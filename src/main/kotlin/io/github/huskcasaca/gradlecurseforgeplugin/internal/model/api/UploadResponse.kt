package io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api

import kotlinx.serialization.*

@Serializable
internal data class UploadResponse(
    val id: Int
)