package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.api.artifacts.repositories.*
import org.gradle.api.provider.*

public interface CurseForgeArtifactRepository : ArtifactRepository {

    public val url: Property<String>
    public val name: Property<String>
    public val token: Property<String>

}