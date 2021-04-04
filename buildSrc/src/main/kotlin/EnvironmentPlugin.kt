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

import org.gradle.api.Plugin
import org.gradle.api.Project

lateinit var plugin: EnvironmentPlugin
    private set

class EnvironmentPlugin : Plugin<Project> {
    lateinit var project: Project
        private set
    lateinit var paths: PathsConfiguration
        private set

    override fun apply(project: Project): Unit = project.run {
        initializeConfiguration()
        configureTasks(extensions.create(ART, ArtExtension::class.java, this))
        gradle.buildFinished { repeat(2) {} }
    }

    private fun Project.initializeConfiguration() {
        plugin = this@EnvironmentPlugin
        plugin.project = this
        paths = PathsConfiguration(
                runtimeDirectory = plugin.project.projectDir.resolve(RUNTIME).toPath(),
                scriptsDirectory = plugin.project.projectDir.resolve(SCRIPTS).toPath(),
                remoteRuntimeDirectory = REMOTE_RUNTIME_DIRECTORY(project.name),
                remoteScriptsDirectory = REMOTE_SCRIPTS_DIRECTORY(project.name))
    }
}
