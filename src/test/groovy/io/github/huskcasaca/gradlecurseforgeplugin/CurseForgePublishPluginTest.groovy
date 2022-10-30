package io.github.huskcasaca.gradlecurseforgeplugin


import io.github.huskcasaca.gradlecurseforgeplugin.internal.publication.DefaultCurseForgePublication
import io.github.huskcasaca.gradlecurseforgeplugin.tasks.GeneratePublicationMetadata
import io.github.huskcasaca.gradlecurseforgeplugin.tasks.PublishToCurseForgeRepository
import org.gradle.api.publish.PublishingExtension

class CurseForgePublishPluginTest extends AbstractProjectBuilderSpec {

    PublishingExtension publishing

    def setup() {
        project.pluginManager.apply(CurseForgePublishPlugin)
        publishing = project.extensions.getByType(PublishingExtension)
    }

    def "no publication by default"() {
        expect:
        publishing.publications.empty
    }

    def "publication can be added"() {
        when:
        publishing.publications.create("test", CurseForgePublication)

        then:
        publishing.publications.size() == 1
        publishing.publications.test instanceof DefaultCurseForgePublication
    }

    def "creates generation tasks for publication"() {
        when:
        publishing.publications.create("test", CurseForgePublication)

        then:
        project.tasks["generateMetadataFilesForTestPublication"] instanceof GeneratePublicationMetadata
    }

    def "creates publish tasks for each publication and repository"() {
        when:
        publishing.publications.create("test", CurseForgePublication)
        publishing.repositories.curseForge.repository("https://foo.com") {}

        then:
        project.tasks["publishTestPublicationToCurseForgeRepository"] instanceof PublishToCurseForgeRepository
    }

    def "tasks are created for compatible publication / repo"() {
        given:
        publishing.publications.create("test", CurseForgePublication)

        when:
        def repo1 = publishing.repositories.curseForge.repository("https://foo.com") {}
        def repo2 = publishing.repositories.curseForge.repository("https://bar.com") { name "other" }
        publishing.repositories.ivy {}

        then:
        publishTasks.size() == 2
        publishTasks.first().repository.is(repo1)
        publishTasks.first().name == "publishTestPublicationToCurseForgeRepository"
        publishTasks.last().repository.is(repo2)
        publishTasks.last().name == "publishTestPublicationToOtherRepository"
    }

    List<PublishToCurseForgeRepository> getPublishTasks() {
        def allTasks = project.tasks.withType(PublishToCurseForgeRepository).sort { it.name }
        return allTasks
    }

    def "creates publish tasks for all publications in a repository"() {
        when:
        publishing.publications.create("test", CurseForgePublication)
        publishing.publications.create("test2", CurseForgePublication)
        publishing.repositories.curseForge.repository("https://foo.com") {}
        publishing.repositories.curseForge.repository("https://bar.com") { name = "other" }

        then:
        project.tasks["publishAllPublicationsToCurseForgeRepository"].dependsOn.containsAll([
            "publishTestPublicationToCurseForgeRepository",
            "publishTest2PublicationToCurseForgeRepository"
        ])
        project.tasks["publishAllPublicationsToOtherRepository"].dependsOn.containsAll([
            "publishTestPublicationToOtherRepository",
            "publishTest2PublicationToOtherRepository"
        ])
    }

}