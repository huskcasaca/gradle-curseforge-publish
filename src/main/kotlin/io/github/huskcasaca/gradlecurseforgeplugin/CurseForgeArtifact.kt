package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.api.JavaVersion
import org.gradle.api.publish.*

public interface CurseForgeArtifact : PublicationArtifact {

    public var displayName: String
    public var releaseType: ReleaseType
    public var changelog: Changelog

    public var loader: LoaderType?
    public var gameVersion: MinecraftVersion?
    public var javaVersion: JavaVersion?

    public val loaders: Set<LoaderType>
    public val gameVersions: Set<MinecraftVersion>
    public val javaVersions: Set<JavaVersion>

    // TODO relations

}