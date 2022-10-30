package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import org.gradle.api.internal.tasks.*
import org.gradle.api.tasks.*
import java.io.*

internal class FileBasedCurseForgeArtifact(
    private val file: File
) : AbstractCurseForgeArtifact() {

    override fun getDefaultBuildDependencies(): TaskDependency = TaskDependencyInternal.EMPTY
    override fun getFile(): File = file

}