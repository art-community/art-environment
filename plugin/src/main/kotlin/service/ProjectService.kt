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

package service

import configuration.ProjectConfiguration
import constants.*
import logger.attention
import logger.error
import logger.infoOutput
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK
import org.eclipse.jgit.api.Git.cloneRepository
import org.eclipse.jgit.api.Git.open
import org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode.YES
import org.eclipse.jgit.lib.TextProgressMonitor
import org.eclipse.jgit.merge.MergeStrategy.THEIRS
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.TagOpt.FETCH_TAGS
import plugin.plugin
import java.io.OutputStreamWriter


fun configureProjects() = plugin.extension.run {
    val settings = buildString {
        appendLine(PROJECTS_NAME_TEMPLATE)
        if (projects.contains(GENERATOR)) {
            appendLine(INCLUDE_BUILD_TEMPLATE(PROJECT_NAMES[GRADLE]!!))
            ProjectConfiguration(GRADLE)
                    .apply { generatorConfiguration.version?.let(::version) }
                    .configure()
        }
        projects.forEach { project ->
            appendLine(INCLUDE_BUILD_TEMPLATE(PROJECT_NAMES[project]!!))
            when (project) {
                JAVA -> javaConfiguration.configure()
                KOTLIN -> kotlinConfiguration.configure()
                TARANTOOL -> tarantoolConfiguration.configure()
                GENERATOR -> generatorConfiguration.configure()
            }
        }
    }
    val project = buildString { append(GRADLE_TASK_TEMPLATE) }
    plugin.paths.apply {
        projectsDirectory.resolve(SETTINGS_GRADLE).write(settings)
        projectsDirectory.resolve(BUILD_GRADLE).write(project)
    }
}

private fun ProjectConfiguration.configure() {
    with(plugin) {
        val projectName = PROJECT_NAMES[name]!!
        val directory = paths.projectsDirectory.resolve(projectName)
        val url = url ?: "${extension.defaultUrl}/$projectName"
        val progressMonitor = TextProgressMonitor(OutputStreamWriter(project.infoOutput()))
        val clone = {
            project.attention("Clone: $url into $directory")
            cloneRepository()
                    .setDirectory(directory.toFile())
                    .setBranch(version)
                    .setURI(url)
                    .setRemote(ORIGIN)
                    .setCloneAllBranches(true)
                    .setCloneSubmodules(true)
                    .setProgressMonitor(progressMonitor)
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
                    project.error("$repository has changes. Please stash it")
                    return
                }
                try {
                    project.attention("Fetch: $directory")
                    fetch()
                            .setRefSpecs(RefSpec(ADD_REFS_HEADS), RefSpec(ADD_REFS_TAGS))
                            .setTagOpt(FETCH_TAGS)
                            .setRemoveDeletedRefs(true)
                            .setForceUpdate(true)
                            .setRecurseSubmodules(YES)
                            .call()
                    version?.let { reference ->
                        project.attention("Checkout: '$version' in $directory")
                        checkout()
                                .setName(reference)
                                .setUpstreamMode(TRACK)
                                .setStartPoint("$ORIGIN/$reference")
                                .setProgressMonitor(progressMonitor)
                                .call()
                    }
                    project.attention("Pull: $directory")
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
