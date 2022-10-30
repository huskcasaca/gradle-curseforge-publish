package io.github.huskcasaca.gradlecurseforgeplugin.internal.publication

import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.AbstractCurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersion
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersionType
import io.github.huskcasaca.gradlecurseforgeplugin.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.*
import org.gradle.api.*
import org.gradle.api.publish.internal.*
import org.gradle.api.tasks.*

internal interface CurseForgePublicationInternal : CurseForgePublication, PublicationInternal<CurseForgeArtifact> {

    val artifact: AbstractCurseForgeArtifact

    val extraArtifacts: Set<AbstractCurseForgeArtifact>

    var publicationMetadataGenerator: TaskProvider<out Task>

    fun isGameVersionIncluded(type: GameVersionType, version: GameVersion): Boolean

}