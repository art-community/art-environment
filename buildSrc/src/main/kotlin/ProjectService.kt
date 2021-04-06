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
import org.gradle.api.Project


const val REFS_HEADS = "refs/heads/"
const val REFS_TAGS = "refs/tags/"
const val ADD_REFS_HEADS = "+refs/heads/*:refs/heads/*"
const val ADD_REFS_TAGS = "+refs/tags/*:refs/tags/*"
const val ORIGIN = "origin"
const val DOT_GIT = ".git"
const val CURRENT_BRANCH_LAST_COMMIT_REV = "HEAD^{tree}"
const val CURRENT_BRANCH_PREVIOUS_COMMIT_REV = "HEAD~1^{tree}"


fun Project.configureProjects() = plugin.extension.run {
    val settingsBuilder = StringBuilder("""rootProject.name = "projects"""").appendln()
    projects.forEach { project ->
        settingsBuilder.append("""includeBuild("$project")""").append("\n")
        when (project) {
            JAVA -> javaConfiguration.configure()
            KOTLIN -> kotlinConfiguration.configure()
            TARANTOOL -> tarantoolConfiguration.configure()
            GENERATOR -> generatorConfiguration.configure()
        }
    }
    plugin.paths.projectsDirectory.resolve("settings.gradle.kts").write(settingsBuilder.toString())
}

private fun ProjectConfiguration.configure() {
    val directory = plugin.paths.projectsDirectory.resolve(name)
    val url = url ?: "${plugin.extension.defaultUrl}/${PROJECT_SOURCES[name]}"
    val clone = {
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
        val hasChanges = repository
                .status()
                .call()
                .uncommittedChanges
                .isNotEmpty()
        if (hasChanges) {
            plugin.project.error("${repository.repository} has changes. Please stash it")
            return
        }
        try {
            repository.fetch()
                    .setRefSpecs(RefSpec(ADD_REFS_HEADS), RefSpec(ADD_REFS_TAGS))
                    .setTagOpt(FETCH_TAGS)
                    .setRemoveDeletedRefs(true)
                    .setForceUpdate(true)
                    .setRecurseSubmodules(YES)
                    .call()
            version?.let { reference ->
                repository
                        .checkout()
                        .setName(reference)
                        .setUpstreamMode(TRACK)
                        .setStartPoint("$ORIGIN/$reference")
                        .call()
            }
            repository.pull()
                    .setFastForward(FF)
                    .setTagOpt(FETCH_TAGS)
                    .setRecurseSubmodules(YES)
                    .setRebase(true)
                    .setRebase(REBASE)
                    .setStrategy(THEIRS)
                    .call()
        } catch (exception: RepositoryNotFoundException) {
            clone()
        }
    }
}
