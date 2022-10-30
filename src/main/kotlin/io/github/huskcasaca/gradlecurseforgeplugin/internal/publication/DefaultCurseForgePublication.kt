package io.github.huskcasaca.gradlecurseforgeplugin.internal.publication

import io.github.huskcasaca.gradlecurseforgeplugin.CurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.AbstractCurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.CurseForgeArtifactNotationParser
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.DerivedCurseForgeArtifact
import io.github.huskcasaca.gradlecurseforgeplugin.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.artifacts.*
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersionType
import io.github.huskcasaca.gradlecurseforgeplugin.internal.model.api.GameVersion
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import org.gradle.api.internal.*
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.attributes.*
import org.gradle.api.internal.component.*
import org.gradle.api.internal.file.*
import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.api.provider.Provider
import org.gradle.api.publish.internal.*
import org.gradle.api.publish.internal.PublicationInternal.PublishedFile
import org.gradle.api.publish.internal.versionmapping.*
import org.gradle.api.tasks.*
import org.gradle.internal.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

internal open class DefaultCurseForgePublication @Inject constructor(
    private val name: String,
    private val artifactFactory: CurseForgeArtifactNotationParser,
    fileCollectionFactory: FileCollectionFactory,
    objects: ObjectFactory,
    collectionCallbackActionDecorator: CollectionCallbackActionDecorator
) : CurseForgePublicationInternal {

    override val id: Property<Int> = objects.property()
    override val loaders: ListProperty<LoaderType> = objects.listProperty()
    override val gameVersions: ListProperty<MinecraftVersion> = objects.listProperty()
    override val javaVersions: ListProperty<JavaVersion> = objects.listProperty()

    private val artifactFileCollection = objects.fileCollection()
    private val artifactDomainObjectSet = objects.domainObjectSet(CurseForgeArtifact::class.java)

    private var _artifact: AbstractCurseForgeArtifact? = null
        set(value) {
            artifactFileCollection.setFrom(value)
            artifactDomainObjectSet.clear()
            artifactDomainObjectSet.add(value)
            field = value
        }

    override val artifact: AbstractCurseForgeArtifact
        get() = _artifact!!

    override val extraArtifacts: Set<AbstractCurseForgeArtifact> = LinkedHashSet()

    // internal called
    override fun artifact(artifact: Any): Unit =
        artifact(artifact) {}

    // public called
    override fun artifact(action: Action<CurseForgeArtifact>) {
        action.execute(artifact)
    }

    // internal called
    override fun artifact(artifact: Any, action: Action<CurseForgeArtifact>) {
        val artifactImpl = artifactFactory.parse(artifact)
        artifactImpl.apply(action::execute)
        _artifact = artifactImpl
    }

    override fun isGameVersionIncluded(type: GameVersionType, version: GameVersion): Boolean {
        return when(type.slug.take(9)) {
            "addons" -> false
            "modloader" -> loaders.get().any { version.slug == it.name.toLowerCase() }
            "minecraft" -> gameVersions.get().any { version.slug == "${it.major}-${it.minor}" + (if (it.patch != 0) "-${it.patch}" else "") + (if (it.snapshot) "-snapshot" else "") }
            "java" -> javaVersions.get().any { version.slug == "java-${it.majorVersion}" }
            else -> false
        }
    }

    private val _javaVersions = mutableSetOf<Any>()
    override var javaVersions_Dep: Set<Any>
        get() = _javaVersions
        set(value) {
            _javaVersions.clear()
            _javaVersions.addAll(value)
        }

    override fun javaVersion(version: Provider<String>) {
        _javaVersions += version
    }

    override fun javaVersion(version: String) {
        _javaVersions += version
    }

    override fun javaVersions(vararg versions: String) {
        _javaVersions.addAll(versions)
    }

    override lateinit var publicationMetadataGenerator: TaskProvider<out Task>

    override fun getName(): String = name

    private var isAlias: Boolean = false
    override fun isAlias(): Boolean = isAlias
    override fun setAlias(alias: Boolean) { isAlias = alias }

    private var withBuildIdentifier: Boolean = false
    override fun withoutBuildIdentifier() { withBuildIdentifier = false }
    override fun withBuildIdentifier() { withBuildIdentifier = true }


    override fun getDisplayName(): DisplayName =
        Describables.withTypeAndName("CurseForge publication", name)

    override fun getCoordinates(): ModuleVersionIdentifier {
        return DefaultModuleVersionIdentifier.newId("com.example", "stub", "0") // TODO Can we figure out a way to properly derive this?
    }

    override fun <T : Any?> getCoordinates(type: Class<T>): T? =
        if (type.isAssignableFrom(ModuleVersionIdentifier::class.java)) {
            type.cast(coordinates)
        } else {
            null
        }

    override fun getComponent(): SoftwareComponentInternal? = null
    override fun isLegacy(): Boolean = false
    override fun getAttributes(): ImmutableAttributes = ImmutableAttributes.EMPTY

    private val derivedArtifacts = DefaultPublicationArtifactSet(CurseForgeArtifact::class.java, "derived artifacts for $name", fileCollectionFactory, collectionCallbackActionDecorator)
    private val publishableArtifacts = CompositePublicationArtifactSet(CurseForgeArtifact::class.java, MainArtifactSetWrapper(), derivedArtifacts)

    override fun getPublishableArtifacts(): PublicationArtifactSet<CurseForgeArtifact> = publishableArtifacts
    override fun allPublishableArtifacts(action: Action<in CurseForgeArtifact>) { publishableArtifacts.all(action) }
    override fun whenPublishableArtifactRemoved(action: Action<in CurseForgeArtifact>) { publishableArtifacts.whenObjectRemoved { action.execute(this) } }

    override fun addDerivedArtifact(originalArtifact: CurseForgeArtifact, file: PublicationInternal.DerivedArtifact): CurseForgeArtifact {
        val artifact = DerivedCurseForgeArtifact(originalArtifact as AbstractCurseForgeArtifact, file)
        derivedArtifacts.add(artifact)

        return artifact
    }

    override fun removeDerivedArtifact(artifact: CurseForgeArtifact) {
        derivedArtifacts.remove(artifact)
    }

    override fun getPublishedFile(source: PublishArtifact?): PublishedFile {
        return object : PublishedFile {
            override fun getName(): String = artifact.file.name
            override fun getUri(): String = "" // TODO Can we figure out a way to properly derive this?
        }
    }

    override fun getVersionMappingStrategy(): VersionMappingStrategyInternal? = null
    override fun isPublishBuildId(): Boolean = false

    private inner class MainArtifactSetWrapper : DelegatingDomainObjectSet<CurseForgeArtifact>(
        CompositeDomainObjectSet.create(CurseForgeArtifact::class.java, artifactDomainObjectSet)
    ), PublicationArtifactSet<CurseForgeArtifact> {
        override fun getFiles(): FileCollection = artifactFileCollection
    }

}