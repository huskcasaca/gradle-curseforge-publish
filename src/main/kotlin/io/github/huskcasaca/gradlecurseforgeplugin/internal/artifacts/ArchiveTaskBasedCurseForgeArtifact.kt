package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import org.gradle.api.internal.tasks.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*
import org.gradle.internal.impldep.com.google.common.collect.*
import java.io.*

internal class ArchiveTaskBasedCurseForgeArtifact(
    private val archiveTask: AbstractArchiveTask
) : AbstractCurseForgeArtifact() {

    private val buildDependencies = DefaultTaskDependency(null, ImmutableSet.of(archiveTask))

    override fun getDefaultBuildDependencies(): TaskDependency = buildDependencies
    override fun getFile(): File = archiveTask.archiveFile.get().asFile

}