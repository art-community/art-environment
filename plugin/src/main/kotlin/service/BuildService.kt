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

package service

import constants.GRADLEW
import constants.GRADLEW_BAT
import constants.isWindows
import logger.line
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.runGradleTasks(projectName: String, vararg tasks: String) {
    val executable = projectsDirectory.resolve(if (isWindows) GRADLEW_BAT else GRADLEW).setExecutable().toFile().absoluteFile.toString()
    val command = arrayOf(executable) + tasks.map { task -> ":$projectName:$task" }
    native(context = project.name) { execute(projectsDirectory, *command) }
    project.line()
}
