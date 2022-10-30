package io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api

import kotlinx.serialization.*

@Serializable
internal data class GameVersionType(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
)