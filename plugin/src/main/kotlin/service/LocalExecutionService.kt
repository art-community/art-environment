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

import constants.LOG_FILE_REFRESH_PERIOD
import logger.attention
import logger.line
import org.zeroturnaround.exec.ProcessExecutor
import plugin.EnvironmentPlugin
import plugin.plugin
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

private val processLogsWriters = mutableMapOf<String, Future<*>>()
private val processLogsExecutor = plugin.register(newSingleThreadScheduledExecutor())

fun EnvironmentPlugin.execute(context: String = project.name, vararg command: String) = execute(context, runtimeDirectory, *command)

fun EnvironmentPlugin.execute(context: String = project.name, directory: Path, vararg command: String) {
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    val processResult = ProcessExecutor()
            .directory(directory.touchDirectory().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(*command)
            .execute()
    consoleLog(output, error, context)
    project.run {
        attention("""Command executed - "${command.joinToString(" ")}" """, context)
        attention("Directory - $directory", context)
        attention("Exit value - ${processResult.exitValue}", context)
    }
}


fun EnvironmentPlugin.bat(name: String, directory: Path = runtimeDirectory, script: () -> String) =
        process(name, directory.resolve(name).bat(), directory, script())

fun EnvironmentPlugin.sh(name: String, directory: Path = runtimeDirectory, script: () -> String) =
        process(name, directory.resolve(name).sh(), directory, script())

private fun EnvironmentPlugin.process(name: String, scriptPath: Path, directory: Path = runtimeDirectory, script: String) {
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
            { processDirectory.localProcessLog(output, error) },
            0,
            LOG_FILE_REFRESH_PERIOD,
            MILLISECONDS
    )
    ProcessExecutor()
            .directory(directory.toFile())
            .redirectOutputAlsoTo(output)
            .redirectErrorAlsoTo(error)
            .command(scriptPath.toAbsolutePath().toString())
            .start()
    project.run {
        attention("Local process started", name)
        attention("Script - $scriptPath", name)
        attention("Output - ${processDirectory.stdout()}", name)
        attention("Error - ${processDirectory.stderr()}", name)
        line()
    }
}


