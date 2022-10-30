package io.github.huskcasaca.gradlecurseforgeplugin

import org.gradle.api.Task
import org.gradle.api.internal.DocumentationRegistry
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.TaskExecuter
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.internal.tasks.execution.DefaultTaskExecutionContext
import org.gradle.api.internal.tasks.properties.DefaultTaskProperties
import org.gradle.api.internal.tasks.properties.PropertyWalker
import org.gradle.execution.ProjectExecutionServices
import org.gradle.internal.execution.BuildOutputCleanupRegistry
import org.gradle.internal.execution.WorkValidationContext
import org.gradle.internal.execution.impl.DefaultWorkValidationContext
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testfixtures.internal.ProjectBuilderImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
abstract class AbstractProjectBuilderSpec extends Specification {

    @TempDir
    @Shared
    File tempDir;

    protected final DocumentationRegistry documentationRegistry = new DocumentationRegistry()

    ProjectInternal project;
    ProjectExecutionServices executionServices;

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(tempDir).build() as ProjectInternal
        executionServices = new ProjectExecutionServices(project)
    }

    def cleanup() {
        ProjectBuilderImpl.stop(project)
    }

    void execute(Task task) {
        def taskExecutionContext = new DefaultTaskExecutionContext(
            null,
            DefaultTaskProperties.resolve(executionServices.get(PropertyWalker), executionServices.get(FileCollectionFactory), task as TaskInternal),
            new DefaultWorkValidationContext(documentationRegistry, WorkValidationContext.TypeOriginInspector.NO_OP),
            { historyMaintained, context -> }
        )
        project.gradle.services.get(BuildOutputCleanupRegistry).resolveOutputs()
        executionServices.get(TaskExecuter).execute((TaskInternal) task, (TaskStateInternal) task.state, taskExecutionContext)
        task.state.rethrowFailure()
    }

}