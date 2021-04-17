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
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.bootstrapTarantool() = extension.tarantoolConfiguration.run {
    instances.asMap.forEach { (name, instance) ->
        when (executionMode) {
            LINUX -> bootstrapLinux(this, name, instance)
            WSL -> bootstrapWsl(this, name, instance)
            REMOTE -> TODO()
        }
    }
}

private fun EnvironmentPlugin.bootstrapLinux(configuration: TarantoolConfiguration, name: String, instance: InstanceConfiguration) {
    val executable = configuration.localExecutionConfiguration.executable ?: TARANTOOL
    val directory = configuration.localExecutionConfiguration.directory
            ?.resolve(name)
            ?.touch()
            ?: paths.runtimeDirectory.resolve(TARANTOOL).resolve(name).touch()
    val scriptPath = directory.resolve(SCRIPTS).touch().resolve(instance.name)
    process(instance.name, scriptPath.sh(), directory) {
        restartLinuxProcess(instance.name, directory) {
            """
                $executable ${scriptPath.lua().writeContent(instance.toLua())} 
            """.trimIndent()
        }
    }
}

private fun EnvironmentPlugin.bootstrapWsl(configuration: TarantoolConfiguration, name: String, instance: InstanceConfiguration) {
    val executable = configuration.localExecutionConfiguration.executable ?: TARANTOOL
    val directory = configuration.localExecutionConfiguration.directory
            ?.resolve(name)
            ?.touch()
            ?: paths.runtimeDirectory.resolve(TARANTOOL).resolve(name).touch()
    val scriptPath = directory.resolve(SCRIPTS).touch().resolve(instance.name)
    process(instance.name, scriptPath.bat(), directory) {
        restartWslProcess(instance.name, directory) {
            """
                wsl -e $executable -- ${scriptPath.lua().writeContent(instance.toLua()).toWsl()} 
            """.trimIndent()
        }
    }
}
