package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.api.*
import org.gradle.api.provider.*
import org.gradle.api.publish.*

public interface CurseForgePublication : Publication {

    public val id: Property<Int>
    public val loaders: ListProperty<LoaderType>
    public val gameVersions: ListProperty<MinecraftVersion>
    public val javaVersions: ListProperty<JavaVersion>

//    public val task: Property<JavaVersion>

    public fun artifact(artifact: Any)

    public fun artifact(action: Action<CurseForgeArtifact>)

    public fun artifact(artifact: Any, action: Action<CurseForgeArtifact>)

    /**
     * The supported Java versions.
     *
     * The set may contain predefined entries. Do not overwrite this value
     * unless you specifically want to overwrite all preset/inferred entries.
     *
     * The set's entries are converted to strings using the [toString][Any.toString]
     * function. Entries of type [Provider] are unwrapped first. The returned
     * string should match the major version component of the supported Java
     * version.
     */
    public var javaVersions_Dep: Set<Any>

    /** Adds the given [version] to the list of [supported Java versions][javaVersions]. */
    public fun javaVersion(version: String)

    /** Adds the given [version] to the list of [supported Java versions][javaVersions]. */
    public fun javaVersion(version: Provider<String>)

    /** Adds the given [versions] to the list of [supported Java versions][javaVersions]. */
    public fun javaVersions(vararg versions: String)

}