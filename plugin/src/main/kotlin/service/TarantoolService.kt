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
import logger.log
import plugin.EnvironmentPlugin
import plugin.plugin
import java.nio.file.Files.copy
import java.nio.file.Path
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

fun EnvironmentPlugin.restartTarantool() = configuration.tarantoolConfiguration.run {
    if (instances.any { instance -> instance.includeModule }) {
        project.run { runGradleTasks(projectName(TARANTOOL), CLEAN, BUILD) }
    }
    instances.forEach { instance ->
        when (executionMode) {
            LOCAL_EXECUTION -> restartOnLocal(this, instance)
            WSL_EXECUTION -> restartOnWsl(this, instance)
            REMOTE_EXECUTION -> restartOnRemote(this, instance)
        }
    }
}

fun EnvironmentPlugin.stopTarantool() = configuration.tarantoolConfiguration.run {
    instances.forEach { instance ->
        when (executionMode) {
            LOCAL_EXECUTION -> stopOnLocal(this, instance.name)
            WSL_EXECUTION -> stopOnWsl(this, instance.name)
            REMOTE_EXECUTION -> stopOnRemote(this, instance.name)
        }
    }
}

fun EnvironmentPlugin.cleanTarantool() = configuration.tarantoolConfiguration.run {
    instances.forEach { instance ->
        when (executionMode) {
            LOCAL_EXECUTION, WSL_EXECUTION -> cleanOnLocal(this, instance.name)
            REMOTE_EXECUTION -> cleanOnRemote(this, instance.name)
        }
    }
}


private fun EnvironmentPlugin.restartOnLocal(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.executionConfiguration.executable ?: TARANTOOL
    val directory = configuration.computeLocalDirectory(instance.name)
    val scriptPath = directory.resolve(instance.name)
    localCopyTarantoolModule(directory, instance)
    printLocalRestartingLog(instance, directory.toString(), executable)
    restartLinuxLocalProcess(instance.name, directory) {
        "$executable ${scriptPath.lua().writeText(instance.toLua())}"
    }
}

private fun EnvironmentPlugin.stopOnLocal(configuration: TarantoolConfiguration, name: String) =
        stopLinuxLocalProcess(name, configuration.computeLocalDirectory(name))


private fun EnvironmentPlugin.restartOnWsl(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.executionConfiguration.executable ?: TARANTOOL
    val directory = configuration.computeLocalDirectory(instance.name)
    val scriptPath = directory.touchDirectory().resolve(instance.name)
    localCopyTarantoolModule(directory, instance)
    printLocalRestartingLog(instance, directory.toString(), executable)
    restartWslProcess(instance.name, directory) {
        "$executable ${scriptPath.lua().writeText(instance.toLua()).toWsl()}"
    }
}

private fun EnvironmentPlugin.stopOnWsl(configuration: TarantoolConfiguration, name: String) =
        stopWslProcess(name, configuration.computeLocalDirectory(name))


private fun EnvironmentPlugin.restartOnRemote(configuration: TarantoolConfiguration, instance: InstanceConfiguration) {
    val executable = configuration.executionConfiguration.executable ?: TARANTOOL
    val directory = configuration.computeRemoteDirectory(instance.name)
    val scriptPath = directory.resolve(instance.name).lua()
    this.configuration.remoteConfiguration.ssh {
        remote {
            printRemoteRestartingLog(instance, directory, executable)
            remoteCopyTarantoolModule(directory, instance)
            restartLinuxRemoteProcess(instance.name, directory) {
                "$executable ${writeFile(scriptPath.lua(), instance.toLua())}"
            }
        }
    }
}

private fun EnvironmentPlugin.stopOnRemote(configuration: TarantoolConfiguration, name: String) = this.configuration.remoteConfiguration.ssh {
    stopLinuxRemoteProcess(name, configuration.computeRemoteDirectory(name))
}


private fun EnvironmentPlugin.cleanOnRemote(configuration: TarantoolConfiguration, name: String) = this.configuration.remoteConfiguration.ssh {
    remote { delete(configuration.computeRemoteDirectory(name)) }
}

private fun EnvironmentPlugin.cleanOnLocal(configuration: TarantoolConfiguration, name: String) = configuration.computeLocalDirectory(name).deleteRecursive()


private fun EnvironmentPlugin.localCopyTarantoolModule(directory: Path, instance: InstanceConfiguration) {
    val destination = directory.resolve(ART_TARANTOOL_LUA).apply { toFile().delete() }
    if (!instance.includeModule) {
        return
    }
    projectDirectory(TARANTOOL)
            .resolve(BUILD)
            .resolve(DESTINATION)
            .resolve(ART_TARANTOOL_LUA)
            .takeIf { lua -> lua.toFile().exists() }
            ?.let { lua -> copy(lua, destination.touchFile(), COPY_ATTRIBUTES, REPLACE_EXISTING) }
}

private fun RemoteExecutionService.remoteCopyTarantoolModule(directory: String, instance: InstanceConfiguration) {
    delete(directory.resolve(ART_TARANTOOL_LUA))
    if (!instance.includeModule) {
        return
    }
    plugin.projectDirectory(TARANTOOL)
            .resolve(BUILD)
            .resolve(DESTINATION)
            .resolve(ART_TARANTOOL_LUA)
            .takeIf { lua -> lua.toFile().exists() }
            ?.let { lua -> writeFile(directory.resolve(lua.toFile().name), lua.toFile().readText()) }
}


private fun TarantoolConfiguration.computeLocalDirectory(name: String): Path {
    val directory = executionConfiguration.localDirectory()?.touchDirectory() ?: plugin.runtimeDirectory
    return directory.resolve(TARANTOOL).resolve(name)
}

private fun TarantoolConfiguration.computeRemoteDirectory(name: String): String {
    val directory = executionConfiguration
            .remoteDirectory()
            ?: plugin.configuration.remoteConfiguration.ssh { remote { runtimeDirectory() } }
    return directory.resolve(TARANTOOL).resolve(name)
}


private fun EnvironmentPlugin.printLocalRestartingLog(instance: InstanceConfiguration, directory: String, executable: String) = project.run {
    log("Tarantool restarting", instance.name)
    log("Directory - $directory", instance.name)
    log("Executable - $executable", instance.name)
    log("Script - \n${instance.toLua().trimIndent()}", instance.name)
}

private fun RemoteExecutionService.printRemoteRestartingLog(instance: InstanceConfiguration, directory: String, executable: String) = plugin.project.run {
    log("Tarantool restarting", context(instance.name))
    log("Directory - $directory", context(instance.name))
    log("Executable - $executable", context(instance.name))
    log("Script - \n${instance.toLua().trimIndent()}", context(instance.name))
}
