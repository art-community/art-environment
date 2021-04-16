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

import constants.ExecutionMode.*
import constants.SCRIPTS
import constants.TARANTOOL
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.bootstrapTarantool() = extension.tarantoolConfiguration.run {
    instances.asMap.forEach { (name, configuration) ->
        when (executionMode) {
            NATIVE -> {
                val executable = localExecutionConfiguration.executable ?: TARANTOOL
                val directory = localExecutionConfiguration.directory
                        ?.resolve(name)
                        ?.touch()
                        ?: paths.runtimeDirectory.resolve(TARANTOOL).resolve(name).touch()
                val script = directory.resolve(SCRIPTS).touch().resolve(configuration.name).lua().writeContent(configuration.lua)
                execute(directory, executable, script.toAbsolutePath().toString())
            }
            WSL -> {
                val executable = localExecutionConfiguration.executable ?: TARANTOOL
                val directory = localExecutionConfiguration.directory?.resolve(name)?.touch()
                        ?: paths.runtimeDirectory.resolve(TARANTOOL).resolve(name).touch()
                val scriptName = directory.resolve(SCRIPTS).touch().resolve(configuration.name)
                val scriptContent = """
                    box.cfg {
                        listen = ${configuration.port},
                        pid_file = "${name}.pid",
                        log_level = 7
                    }
                """.trimIndent()
                process(configuration.name, scriptName.bat(), directory) {
                    buildString {
                        val pidFile = directory.resolve(configuration.name).pid()
                                .toFile()
                                .takeIf { file -> file.exists() }
                        val pid = pidFile
                                ?.readText()
                                ?.takeIf { string -> string.isNotBlank() }
                                ?.toInt()
                        pid?.apply { appendLine("""wsl -e kill -- -9 $this""") }
                        val file = directory.resolve(configuration.name).pid().toFile()
                        file.delete()
                        file.createNewFile()
                        appendLine("""wsl -e $executable -- ${scriptName.lua().writeContent(scriptContent).toWsl()} """)
                    }
                }
            }
            REMOTE -> TODO()
        }
    }
}
