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

import configuration.TarantoolConfiguration
import configuration.TarantoolConfiguration.InstanceConfiguration
import constants.*
import constants.ExecutionMode.*
import logger.attention
import plugin.EnvironmentPlugin
import java.nio.file.Path

fun EnvironmentPlugin.restartTarantool() = extension.tarantoolConfiguration.run {
    if (instances.any { instance -> instance.includeModule }) {
        project.run { runGradleTasks(projectName(TARANTOOL), CLEAN, BUILD) }
    }
    instances.asMap.values.forEach { instance ->
        when (executionMode) {
            LOCAL -> restartOnLocal(this, instance)
            WSL -> restartOnWsl(this, instance)
            REMOTE -> TODO()
        }
    }
}

fun EnvironmentPlugin.stopTarantool() = extension.tarantoolConfiguration.run {
    instances.asMap.keys.forEach { name ->
        when (executionMode) {
            LOCAL -> stopOnLocal(this, name)
            WSL -> stopOnWsl(this, name)
            REMOTE -> TODO()
        }
    }
}


private fun EnvironmentPlugin.restartOnLocal(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.executionConfiguration.executable ?: TARANTOOL
    val directory = configuration.computeDirectory(instance.name)
    val scriptPath = directory.touchDirectory().resolve(instance.name)
    copyTarantoolModule(directory, instance)
    restartingLog(instance, directory, executable)
    restartLinuxProcess(instance.name, directory) { "$executable ${scriptPath.lua().writeText(instance.toLua())}" }
}

private fun EnvironmentPlugin.stopOnLocal(configuration: TarantoolConfiguration, name: String) =
        stopLinuxProcess(name, configuration.computeDirectory(name))

private fun EnvironmentPlugin.restartOnWsl(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.executionConfiguration.executable ?: TARANTOOL
    val directory = configuration.computeDirectory(instance.name)
    val scriptPath = directory.touchDirectory().resolve(instance.name)
    copyTarantoolModule(directory, instance)
    restartingLog(instance, directory, executable)
    restartWslProcess(instance.name, directory) { "wsl -e $executable -- ${scriptPath.lua().writeText(instance.toLua()).toWsl()}" }
}

private fun EnvironmentPlugin.stopOnWsl(configuration: TarantoolConfiguration, name: String) =
        stopWslProcess(name, configuration.computeDirectory(name))


private fun EnvironmentPlugin.copyTarantoolModule(directory: Path, instance: InstanceConfiguration) {
    project.run {
        directory.resolve(ART_TARANTOOL_LUA).toFile().delete()
        if (!instance.includeModule) {
            return
        }
        copy {
            projectDirectory(TARANTOOL)
                    .resolve(BUILD)
                    .resolve(DESTINATION)
                    .resolve(ART_TARANTOOL_LUA)
                    .takeIf { lua -> lua.toFile().exists() }
                    ?.let { lua -> from(lua); into(directory) }
        }
    }
}

private fun EnvironmentPlugin.restartingLog(instance: InstanceConfiguration, directory: Path, executable: String) = project.run {
    attention("Tarantool restarting", instance.name)
    attention("Directory - $directory", instance.name)
    attention("Executable - $executable", instance.name)
    attention("Script - \n${instance.toLua()}", instance.name)
}

private fun TarantoolConfiguration.computeDirectory(name: String): Path = executionConfiguration.directory()
        .resolve(TARANTOOL)
        .resolve(name)
