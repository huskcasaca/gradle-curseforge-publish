package io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api

import kotlinx.serialization.*

@Serializable
internal class GameVersion(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("slug") val slug: String,
    @SerialName("gameVersionTypeID") val gameVersionTypeID: Int,
    @SerialName("apiVersion") val apiVersion: String?

)