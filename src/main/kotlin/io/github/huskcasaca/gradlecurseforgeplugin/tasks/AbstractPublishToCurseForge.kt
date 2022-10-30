package io.github.huskcasaca.gradlecurseforgeplugin.tasks

import io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.CurseForgePublicationInternal
import io.github.huskcasaca.gradlecurseforgeplugin.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.work.*
import java.util.concurrent.*

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
public abstract class AbstractPublishToCurseForge : DefaultTask() {

    private var _publication: CurseForgePublicationInternal? = null

    @get:Internal
    internal var publication: CurseForgePublication?
        get() = _publication
        set(value) { _publication = value.asPublicationInternal() }

    @get:Internal
    internal val publicationInternal: CurseForgePublicationInternal?
        get() = _publication

    init {
        // Allow the publication to participate in incremental build
        inputs.files(Callable { publicationInternal?.publishableArtifacts?.files })
            .withPropertyName("publication.publishableFiles")
            .withPathSensitivity(PathSensitivity.NAME_ONLY)
    }

    private fun CurseForgePublication?.asPublicationInternal(): CurseForgePublicationInternal? = when (this) {
        null -> null
        is CurseForgePublicationInternal -> this
        else -> throw InvalidUserDataException(
            "Publication objects must implement the '${io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.CurseForgePublicationInternal::class.qualifiedName}' interface, implementation '${this::class.qualifiedName}' does not"
        )
    }

}