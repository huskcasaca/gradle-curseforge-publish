package io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts

import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersion
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersionType
import org.gradle.api.JavaVersion
import org.gradle.api.internal.tasks.*
import org.gradle.api.tasks.*

internal abstract class AbstractCurseForgeArtifact : CurseForgeArtifact {

    private val allBuildDependencies = CompositeTaskDependency()
    private val additionalBuildDependencies = DefaultTaskDependency()

    override lateinit var displayName: String
    override lateinit var releaseType: ReleaseType
    override lateinit var changelog: Changelog

    internal val displayNameSafe: String?
        get() {
            if (this::displayName.isInitialized) {
                return displayName
            } else {
                return null
            }
        }

    override var loader: LoaderType?
        get() {
            val loaders = this.loaders
            return if (loaders.size == 1) loaders.first() else null
        }
        set(value) {
            if (value != null) {
                loaders += value
            } else {
                loaders.clear()
            }
        }
    override var gameVersion: MinecraftVersion?
        get() {
            val gameVersions = this.gameVersions
            return if (gameVersions.size == 1) gameVersions.first() else null
        }
        set(value) {
            if (value != null) {
                gameVersions += value
            } else {
                gameVersions.clear()
            }
        }
    override var javaVersion: JavaVersion?
        get() {
            val javaVersions = this.javaVersions
            return if (javaVersions.size == 1) javaVersions.first() else null
        }
        set(value) {
            if (value != null) {
                javaVersions += value
            } else {
                javaVersions.clear()
            }
        }

    override val loaders: MutableSet<LoaderType> = mutableSetOf()
    override val gameVersions: MutableSet<MinecraftVersion> = mutableSetOf()
    override val javaVersions: MutableSet<JavaVersion> = mutableSetOf()

    override fun builtBy(vararg tasks: Any) {
        additionalBuildDependencies.add(tasks)
    }

    override fun getBuildDependencies(): TaskDependency =
        allBuildDependencies

    abstract fun getDefaultBuildDependencies(): TaskDependency

    private inner class CompositeTaskDependency : AbstractTaskDependency() {

        override fun visitDependencies(context: TaskDependencyResolveContext) {
            context.add(getDefaultBuildDependencies())
            additionalBuildDependencies.visitDependencies(context)
        }
    }

    internal fun isGameVersionIncluded(type: GameVersionType, version: GameVersion): Boolean {
        return when(type.slug.take(9)) {
            "addons" -> false
            "modloader" -> loaders.any { version.slug == it.name.toLowerCase() }
            "minecraft" -> gameVersions.any { version.slug == "${it.major}-${it.minor}" + (if (it.patch != 0) "-${it.patch}" else "") + (if (it.snapshot) "-snapshot" else "") }
            "java" -> javaVersions.any { version.slug == "java-${it.majorVersion}" }
            else -> false
        }
    }

}