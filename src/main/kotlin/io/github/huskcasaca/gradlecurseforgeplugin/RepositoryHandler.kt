package io.github.huskcasaca.gradlecurseforgeplugin

import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.repositories.DefaultCurseForgeArtifactRepository
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.repositories.*
import io.github.huskcasaca.gradlecurseforgeplugin.plugins.*
import org.gradle.api.*
import org.gradle.api.artifacts.dsl.*

public fun RepositoryHandler.curseForge(action: Action<in CurseForgeArtifactRepository>): CurseForgeArtifactRepository =
    curseForge("https://minecraft.curseforge.com", action)

public fun RepositoryHandler.curseForge(url: String, action: Action<in CurseForgeArtifactRepository>): CurseForgeArtifactRepository =
    CurseForgePublishPlugin.gradle.rootProject.objects.newInstance(DefaultCurseForgeArtifactRepository::class.java, url)
        .also(action::execute)
        .also(::add)