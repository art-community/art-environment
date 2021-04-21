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

import logger.attention
import plugin.EnvironmentPlugin
import plugin.plugin
import java.nio.file.Path

fun EnvironmentPlugin.stopWslProcess(name: String, directory: Path) {
    directory.resolve(name)
            .pid()
            .toFile()
            .takeIf { pid -> pid.exists() }
            ?.apply {
                readText().takeIf { pid -> pid.isNotBlank() }
                        ?.toInt()
                        ?.let { pid ->
                            wsl { kill(pid) }
                            project.attention("WSL process killed $pid", name)
                        }
                delete()
            }
}

fun EnvironmentPlugin.stopLinuxLocalProcess(name: String, directory: Path) {
    directory.resolve(name)
            .pid()
            .toFile()
            .takeIf { pid -> pid.exists() }
            ?.apply {
                readText().takeIf { pid -> pid.isNotBlank() }
                        ?.toInt()
                        ?.let { pid ->
                            native { kill(pid) }
                            project.attention("Linux process killed $pid", name)
                        }
                delete()
            }

}

fun RemoteClient.stopLinuxRemoteProcess(name: String, directory: String) {
    remote(context = name) {
        directory.resolve(name)
                .pid()
                .takeIf(::fileExists)
                ?.apply {
                    readFile(this).takeIf { pid -> pid.isNotBlank() }
                            ?.toInt()
                            ?.let { pid ->
                                kill(pid)
                                plugin.project.attention("Linux process killed $pid", context())
                            }
                    delete(this)

                }
    }
}


fun EnvironmentPlugin.restartWslProcess(name: String, directory: Path, script: () -> String) {
    stopWslProcess(name, directory)
    wsl { bat(name, directory, script) }
}

fun EnvironmentPlugin.restartLinuxLocalProcess(name: String, directory: Path, script: () -> String) {
    stopLinuxLocalProcess(name, directory)
    native { sh(name, directory, script) }
}

fun RemoteClient.restartLinuxRemoteProcess(name: String, directory: String, script: () -> String) {
    stopLinuxRemoteProcess(name, directory)
    remote { sh(name, directory, script) }
}