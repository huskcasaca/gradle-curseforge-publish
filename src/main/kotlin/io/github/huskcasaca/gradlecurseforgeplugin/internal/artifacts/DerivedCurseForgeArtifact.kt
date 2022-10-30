package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import io.github.huskcasaca.gradlecurseforgeplugin.*
import org.gradle.api.internal.tasks.*
import org.gradle.api.publish.internal.*
import org.gradle.api.tasks.*
import java.io.*

internal class DerivedCurseForgeArtifact(
    original: AbstractCurseForgeArtifact,
    private val derivedFile: PublicationInternal.DerivedArtifact
) : AbstractCurseForgeArtifact() {

    override var displayName: String = original.displayName
    override var changelog: Changelog = original.changelog
    override var releaseType: ReleaseType = original.releaseType

    override fun getDefaultBuildDependencies(): TaskDependency =
        TaskDependencyInternal.EMPTY

    override fun getFile(): File =
        derivedFile.create()!!

}