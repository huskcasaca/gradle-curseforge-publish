package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import org.gradle.api.artifacts.*
import org.gradle.api.internal.artifacts.dsl.*
import org.gradle.api.internal.file.*
import org.gradle.api.internal.tasks.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.bundling.*

internal class CurseForgeArtifactNotationParser(
    private val fileResolver: FileResolver
) {

    fun parse(any: Any): AbstractCurseForgeArtifact = when (any) {
        is AbstractArchiveTask -> parseArchiveTaskNotation(any)
        is Provider<*> -> parseProviderNotation(any)
        is PublishArtifact -> parsePublishArtifactNotation(any)
        else -> parseFileNotation(any) ?: error("Failed to parse artifact notation: $any")
    }

    private fun parseArchiveTaskNotation(archiveTask: AbstractArchiveTask): AbstractCurseForgeArtifact =
        ArchiveTaskBasedCurseForgeArtifact(archiveTask)

    private fun parseFileNotation(notation: Any): AbstractCurseForgeArtifact? {
        val file = runCatching { fileResolver.asNotationParser().parseNotation(notation) }.getOrNull() ?: return null
        val artifact = FileBasedCurseForgeArtifact(file)

        if (notation is TaskDependencyContainer) {
            artifact.builtBy(
                if (notation is Provider<*>)
                    TaskDependencyContainer { context -> context.add(notation) }
                else
                    notation
            )
        }

        return artifact
    }

    private fun parseProviderNotation(provider: Provider<*>): AbstractCurseForgeArtifact =
        PublishArtifactBasedCurseForgeArtifact(LazyPublishArtifact(provider, fileResolver))

    private fun parsePublishArtifactNotation(publishArtifact: PublishArtifact): AbstractCurseForgeArtifact =
        PublishArtifactBasedCurseForgeArtifact(publishArtifact)

}