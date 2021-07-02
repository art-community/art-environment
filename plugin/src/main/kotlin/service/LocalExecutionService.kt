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

import constants.*
import logger.line
import logger.log
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import plugin.EnvironmentPlugin
import plugin.plugin
import service.LocalExecutionMode.NATIVE_COMMAND
import service.LocalExecutionMode.WSL_COMMAND
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

private val processLogsWriters = mutableMapOf<String, Future<*>>()
private val processLogsExecutor = plugin.register(newSingleThreadScheduledExecutor())

enum class LocalExecutionMode {
    NATIVE_COMMAND,
    WSL_COMMAND
}

class LocalExecutionService(private var trace: Boolean, private var context: String, private val mode: LocalExecutionMode) {
    fun trace(trace: Boolean = true) {
        this.trace = trace
    }

    fun trace(trace: Boolean = true, context: String = this.context) {
        this.trace = trace
        this.context = context
    }

    fun execute(vararg command: String) = execute(plugin.runtimeDirectory, *command)

    fun execute(directory: Path, vararg command: String): ProcessResult {
        val arguments = when (mode) {
            NATIVE_COMMAND -> command
            WSL_COMMAND -> if (command.size == 1) wslCommand(command[0]) else wslCommand(command[0], *command.drop(1).toTypedArray())
        }
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val processResult = ProcessExecutor()
                .directory(directory.touchDirectory().toFile())
                .redirectOutput(output)
                .redirectError(error)
                .command(*arguments)
                .execute()
        if (!trace) return processResult
        plugin.printToConsole(output, error, context)
        plugin.project.run {
            log("""Command executed - "${arguments.joinToString(SPACE)}" """, context)
            log("Directory - $directory", context)
            log("Exit value - ${processResult.exitValue}", context)
        }
        return processResult
    }

    fun bat(name: String, directory: Path = plugin.runtimeDirectory, script: () -> String) =
            process(name, directory.resolve(name).bat(), directory, script())

    fun sh(name: String, directory: Path = plugin.runtimeDirectory, script: () -> String) =
            process(name, directory.resolve(name).sh(), directory, script())

    private fun process(name: String, scriptPath: Path, directory: Path = plugin.runtimeDirectory, script: String): StartedProcess {
        val processDirectory = directory.resolve(name)
        runCatching {
            processLogsWriters.remove(name)?.cancel(true)
            processDirectory.stdout().clear()
            processDirectory.stderr().clear()
        }
        directory.resolve(scriptPath).writeText(script.trimIndent()).setExecutable()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        processLogsWriters[name] = processLogsExecutor.scheduleAtFixedRate(
                { processDirectory.printToFile(output, error) },
                0,
                LOG_FILE_REFRESH_PERIOD,
                MILLISECONDS
        )
        when (mode) {
            NATIVE_COMMAND -> {
                val process = ProcessExecutor()
                        .directory(directory.toFile())
                        .redirectOutputAlsoTo(output)
                        .redirectErrorAlsoTo(error)
                        .command(scriptPath.toAbsolutePath().toString())
                        .start()
                plugin.project.run {
                    log("Local process started", context)
                    log("Script - $scriptPath", context)
                    log("Output - ${processDirectory.stdout()}", context)
                    log("Error - ${processDirectory.stderr()}", context)
                    line()
                    return process
                }
            }
            WSL_COMMAND -> {
                val process = ProcessExecutor()
                        .directory(directory.toFile())
                        .redirectOutputAlsoTo(output)
                        .redirectErrorAlsoTo(error)
                        .command(WSL, BASH, scriptPath.toWsl())
                        .start()
                plugin.project.run {
                    log("WSL process started", name)
                    log("Script - $scriptPath", name)
                    log("Output - ${processDirectory.stdout()}", name)
                    log("Error - ${processDirectory.stderr()}", name)
                    line()
                    return process
                }
            }
        }
    }
}

fun <T> EnvironmentPlugin.native(trace: Boolean = project.logger.isTraceEnabled, context: String = project.name, service: LocalExecutionService.() -> T) =
        service(LocalExecutionService(trace, context, NATIVE_COMMAND))

fun <T> EnvironmentPlugin.wsl(trace: Boolean = project.logger.isTraceEnabled, context: String = project.name, service: LocalExecutionService.() -> T): T =
        service(LocalExecutionService(trace, context, WSL_COMMAND))

fun wslCommand(executable: String, vararg arguments: String) = when {
    arguments.isEmpty() -> arrayOf(WSL, E_ARGUMENT, executable)
    else -> arrayOf(WSL, E_ARGUMENT, executable, PASS_ARGUMENTS) + arguments
}
