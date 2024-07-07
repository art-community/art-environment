package service

import constants.ART
import constants.MAIN
import constants.RELEASE
import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import plugin.plugin
import kotlin.io.path.exists

fun Project.configureReleasing() {
    plugin.run {
        if (configuration.releaseConfiguration.version != null) {
            tasks.register(RELEASE) {
                group = ART
                doLast {
                    configuration.projects.forEach { project ->
                        logger.quiet("Releasing ${projectName(project)} with tag ${configuration.releaseConfiguration.version}")
                        val projectName = projectName(project)
                        val directory = projectsDirectory.resolve(projectName)
                        if (!directory.exists()) {
                            logger.quiet("Skipping ${projectName(project)}. Directory not found")
                            return@forEach
                        }
                        val repository = Git.open(directory.toFile())
                        if (repository.repository.branch != MAIN) {
                            logger.quiet("Skipping ${projectName(project)}. Branch is not main")
                            return@forEach
                        }
                        if (repository.status().call().uncommittedChanges.isNotEmpty()) {
                            logger.quiet("Skipping ${projectName(project)}. Has uncommitted changes")
                            return@forEach
                        }
                        if (repository.tagList().call().any { ref -> ref.name == "refs/tags/${configuration.releaseConfiguration.version}" }) {
                            logger.quiet("Skipping ${projectName(project)}. Version exists")
                            return@forEach

                        }
                        repository.tag().setName(configuration.releaseConfiguration.version).setForceUpdate(true).call()
                        exec {
                            workingDir = directory.toFile()
                            commandLine("git", "push", "--tags")
                        }
                        repository.close()
                    }
                }

            }
        }
    }
}