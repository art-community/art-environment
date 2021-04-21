/*
 * ART
 *
 * Copyright 2019-2021 ART
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
import logger.attention
import logger.logger
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK
import org.eclipse.jgit.api.Git.cloneRepository
import org.eclipse.jgit.api.Git.open
import org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.SubmoduleConfig.FetchRecurseSubmodulesMode.YES
import org.eclipse.jgit.merge.MergeStrategy.THEIRS
import org.eclipse.jgit.transport.TagOpt.FETCH_TAGS
import plugin.EnvironmentPlugin
import service.*

fun EnvironmentPlugin.configureProjects() = extension.apply {
    val settings = buildString {
        appendLine(PROJECTS_NAME_TEMPLATE)
        appendLine(INCLUDE_BUILD_TEMPLATE(projectName(GRADLE_PLUGIN)))
        configureProject(gradlePluginConfiguration)
        projects.forEach { project ->
            appendLine(INCLUDE_BUILD_TEMPLATE(projectName(project)))
            when (project) {
                JAVA -> configureProject(javaConfiguration)
                KOTLIN -> configureProject(kotlinConfiguration)
                TARANTOOL -> configureProject(tarantoolConfiguration)
                GENERATOR -> configureProject(generatorConfiguration)
                EXAMPLE -> configureProject(exampleConfiguration)
            }
        }
        if (projects.contains(SANDBOX)) configureSandbox()
    }
    val buildTemplate = buildString { append(PROJECTS_GRADLE_BUILD_TEMPLATE) }
    projectsDirectory.resolve(SETTINGS_GRADLE).writeText(settings)
    projectsDirectory.resolve(BUILD_GRADLE).writeText(buildTemplate)
    environmentDirectory.parent.resolve(GRADLE).copyRecursive(projectDirectory(GRADLE_PLUGIN).resolve(GRADLE))
    environmentDirectory.parent.resolve(GRADLEW).copyRecursive(projectsDirectory.resolve(GRADLEW))
    environmentDirectory.parent.resolve(GRADLEW).copyRecursive(projectsDirectory.resolve(GRADLEW_BAT))
}


private fun EnvironmentPlugin.configureSandbox() {
    val directory = projectsDirectory.resolve(SANDBOX)
    if (directory.toFile().exists()) {
        project.attention("Sandbox configured: $directory")
        return
    }
    directory.touchDirectory()
    directory.resolve(SETTINGS_GRADLE).writeText(SANDBOX_SETTINGS_TEMPLATE)
    directory.resolve(BUILD_GRADLE).writeText(SANDBOX_BUILD_TEMPLATE)
    environmentDirectory.parent.resolve(GRADLE).copyRecursive(directory.resolve(GRADLE))
    environmentDirectory.parent.resolve(GRADLEW).copyRecursive(directory.resolve(GRADLEW))
    environmentDirectory.parent.resolve(GRADLEW).copyRecursive(directory.resolve(GRADLEW_BAT))
    project.attention("Sandbox configured: $directory")
}

private fun EnvironmentPlugin.configureProject(configuration: ProjectConfiguration) = configuration.run {
    val projectName = projectName(configuration.name)
    val directory = projectsDirectory.resolve(projectName)
    val url = url ?: "${extension.defaultUrl}/$projectName"
    val version = version ?: MAIN
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
        return@run
    }
    open(directory.toFile()).use { repository ->
        repository.apply {
            if (status().call().uncommittedChanges.isNotEmpty()) {
                logger.error("Changes detected. Please stash, revert or commit them")
                return@run
            }
            try {
                logger.attention("Fetch")
                fetch()
                        .setTagOpt(FETCH_TAGS)
                        .setRefSpecs(ADD_REFS_HEADS, ADD_REFS_TAGS)
                        .setRemoveDeletedRefs(true)
                        .setForceUpdate(true)
                        .setRecurseSubmodules(YES)
                        .call()

                logger.attention("Checkout '$version'")
                checkout()
                        .setName(version)
                        .setUpstreamMode(TRACK)
                        .setStartPoint("$ORIGIN/$version")
                        .call()

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
