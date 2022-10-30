package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.api.*
import org.gradle.api.artifacts.dsl.*
import javax.inject.*

public abstract class CurseForgeRepositoryExtension @Inject constructor(
    private val repositories: RepositoryHandler
) {

    public fun repository(action: Action<CurseForgeArtifactRepository>): CurseForgeArtifactRepository =
        repositories.curseForge(action)

    public fun repository(url: String, action: Action<CurseForgeArtifactRepository>): CurseForgeArtifactRepository =
        repositories.curseForge(url, action)

}