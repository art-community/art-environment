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
import constants.ExecutionMode.*
import constants.SCRIPTS
import constants.TARANTOOL
import logger.attention
import plugin.EnvironmentPlugin
import java.nio.file.Path

fun EnvironmentPlugin.bootstrapTarantool() = extension.tarantoolConfiguration.run {
    instances.asMap.values.forEach { instance ->
        when (executionMode) {
            LINUX -> bootstrapLinux(this, instance)
            WSL -> bootstrapWsl(this, instance)
            REMOTE -> TODO()
        }
    }
}

fun EnvironmentPlugin.killTarantool() = extension.tarantoolConfiguration.run {
    instances.asMap.keys.forEach { name ->
        when (executionMode) {
            LINUX -> killLinux(this, name)
            WSL -> killWsl(this, name)
            REMOTE -> TODO()
        }
    }
}


private fun EnvironmentPlugin.bootstrapLinux(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.localExecutionConfiguration.executable ?: TARANTOOL
    val directory = computeDirectory(configuration, instance.name)
    val scriptPath = directory.resolve(SCRIPTS).touchDirectory().resolve(instance.name)
    bootstrapLog(instance, directory, executable)
    restartLinuxProcess(instance.name, directory) { "$executable ${scriptPath.lua().writeContent(instance.toLua())}" }
}

private fun EnvironmentPlugin.killLinux(configuration: TarantoolConfiguration, name: String) = stopLinuxProcess(name, computeDirectory(configuration, name))


private fun EnvironmentPlugin.bootstrapWsl(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.localExecutionConfiguration.executable ?: TARANTOOL
    val directory = computeDirectory(configuration, instance.name)
    val scriptPath = directory.resolve(SCRIPTS).touchDirectory().resolve(instance.name)
    bootstrapLog(instance, directory, executable)
    restartWslProcess(instance.name, directory) { "wsl -e $executable -- ${scriptPath.lua().writeContent(instance.toLua()).toWsl()}" }
}

private fun EnvironmentPlugin.killWsl(configuration: TarantoolConfiguration, name: String) = stopWslProcess(name, computeDirectory(configuration, name))


private fun EnvironmentPlugin.bootstrapLog(instance: InstanceConfiguration, directory: Path, executable: String) = project.run {
    attention("Tarantool bootstrap", instance.name)
    attention("Directory: $directory", instance.name)
    attention("Executable: $executable", instance.name)
    attention("Script: ${instance.toLua()}", instance.name)
}

private fun EnvironmentPlugin.computeDirectory(configuration: TarantoolConfiguration, name: String): Path {
    return configuration.localExecutionConfiguration.directory
            ?.resolve(name)
            ?.touchDirectory()
            ?: paths.runtimeDirectory.resolve(TARANTOOL).resolve(name).touchDirectory()
}
