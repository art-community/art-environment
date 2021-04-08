/*
 * ART
 *
 * Copyright 2020 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configurator

import configuration.ProjectConfiguration
import constants.*
import logger.logger
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK
import org.eclipse.jgit.api.Git.cloneRepository
import org.eclipse.jgit.api.Git.open
import org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode.YES
import org.eclipse.jgit.merge.MergeStrategy.THEIRS
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.TagOpt.FETCH_TAGS
import plugin.plugin
import service.write
import java.nio.file.Files.copy
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

fun configureProjects() = plugin.extension.run {
    val settings = buildString {
        appendLine(PROJECTS_NAME_TEMPLATE)
        if (projects.contains(EXAMPLE)) {
            appendLine(INCLUDE_BUILD_TEMPLATE(PROJECT_NAMES[GRADLE_PLUGIN]!!))
            when (projects.contains(GRADLE_PLUGIN)) {
                true -> gradlePluginConfiguration.configure()
                false -> ProjectConfiguration(GRADLE_PLUGIN).apply { generatorConfiguration.version?.let(::version) }.configure()
            }
        }
        projects.forEach { project ->
            appendLine(INCLUDE_BUILD_TEMPLATE(PROJECT_NAMES[project]!!))
            when (project) {
                JAVA -> javaConfiguration.configure()
                KOTLIN -> kotlinConfiguration.configure()
                TARANTOOL -> tarantoolConfiguration.configure()
                GENERATOR -> generatorConfiguration.configure()
                EXAMPLE -> exampleConfiguration.configure()
            }
        }
    }
    val project = buildString { append(GRADLE_TASK_TEMPLATE) }
    plugin.paths.apply {
        projectsDirectory.resolve(SETTINGS_GRADLE).write(settings)
        projectsDirectory.resolve(BUILD_GRADLE).write(project)
        copy(plugin.project.rootDir.resolve(GRADLE).toPath(), projectsDirectory.resolve(GRADLE), REPLACE_EXISTING)
        copy(plugin.project.rootDir.resolve(GRADLEW).toPath(), projectsDirectory.resolve(GRADLEW), REPLACE_EXISTING)
        copy(plugin.project.rootDir.resolve(GRADLEW_BAT).toPath(), projectsDirectory.resolve(GRADLEW_BAT), REPLACE_EXISTING)
    }
}

private fun ProjectConfiguration.configure() {
    with(plugin) {
        val projectName = PROJECT_NAMES[name]!!
        val directory = paths.projectsDirectory.resolve(projectName)
        val url = url ?: "${extension.defaultUrl}/$projectName"
        val logger = project.logger(projectName)
        val clone = {
            logger.attention("Clone $url")
            cloneRepository()
                    .setDirectory(directory.toFile())
                    .setBranch(version)
                    .setURI(url)
                    .setRemote(ORIGIN)
                    .setCloneAllBranches(true)
                    .setCloneSubmodules(true)
                    .call()
                    .close()
        }
        if (!directory.toFile().exists()) {
            clone()
            return
        }
        open(directory.toFile()).use { repository ->
            with(repository) {
                if (status().call().uncommittedChanges.isNotEmpty()) {
                    logger.error("Changes detected. Please stash, revert or commit them")
                    return
                }
                try {
                    logger.attention("Fetch")
                    fetch()
                            .setRefSpecs(RefSpec(ADD_REFS_HEADS), RefSpec(ADD_REFS_TAGS))
                            .setTagOpt(FETCH_TAGS)
                            .setRemoveDeletedRefs(true)
                            .setForceUpdate(true)
                            .setRecurseSubmodules(YES)
                            .call()
                    version?.let { reference ->
                        logger.attention("Checkout '$version'")
                        checkout()
                                .setName(reference)
                                .setUpstreamMode(TRACK)
                                .setStartPoint("$ORIGIN/$reference")
                                .call()
                    }
                    logger.attention("Pull")
                    pull()
                            .setFastForward(FF)
                            .setTagOpt(FETCH_TAGS)
                            .setRecurseSubmodules(YES)
                            .setRebase(true)
                            .setRebase(REBASE)
                            .setRemote(ORIGIN)
                            .setStrategy(THEIRS)
                            .call()
                } catch (exception: RepositoryNotFoundException) {
                    clone()
                }
            }
        }
    }
}
