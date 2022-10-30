package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import org.gradle.api.artifacts.*
import org.gradle.api.tasks.*
import java.io.*

internal class PublishArtifactBasedCurseForgeArtifact(
    private val publishArtifact: PublishArtifact
) : AbstractCurseForgeArtifact() {

    override fun getDefaultBuildDependencies(): TaskDependency = publishArtifact.buildDependencies
    override fun getFile(): File = publishArtifact.file

}