package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.repositories

import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifactRepository
import org.gradle.api.*
import org.gradle.api.artifacts.repositories.*
import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

internal open class DefaultCurseForgeArtifactRepository @Inject constructor(
    url: String,
    objectFactory: ObjectFactory
) : CurseForgeArtifactRepository {

    override val name: Property<String> = objectFactory.property<String>().apply { set("CurseForge") }
    override val url: Property<String> = objectFactory.property<String>().apply { set(url) }
    override val token: Property<String> = objectFactory.property()

    override fun getName(): String = name.get()
    override fun setName(name: String) { this.name.set(name) }

    override fun content(configureAction: Action<in RepositoryContentDescriptor>) {
        error("Unsupported Operation")
    }

}