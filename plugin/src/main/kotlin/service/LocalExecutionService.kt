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

import constants.NEW_LINE
import logger.attention
import logger.error
import org.zeroturnaround.exec.ProcessExecutor
import plugin.EnvironmentPlugin
import plugin.plugin
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

private val processLogsWriters = mutableMapOf<String, Future<*>>()

fun EnvironmentPlugin.execute(vararg command: String) = execute(plugin.paths.runtimeDirectory, *command)

fun EnvironmentPlugin.execute(directory: Path, vararg command: String) {
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    val processResult = ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(*command)
            .execute()
    project.run {
        attention("Command executed")
        attention("Directory: $directory")
        attention("Exit code: ${processResult.exitValue}")
        attention("Output: ${processResult.outputString()}")
        attention("Command ${command.joinToString(" ")}")
    }
    logExecution(output, error)
}


fun EnvironmentPlugin.execute(path: Path, script: () -> String) = execute(path, plugin.paths.runtimeDirectory, script)

fun EnvironmentPlugin.execute(path: Path, script: String) = execute(path, plugin.paths.runtimeDirectory, script)


fun EnvironmentPlugin.execute(path: Path, directory: Path = plugin.paths.runtimeDirectory, script: () -> String) = execute(path, directory, script())

fun EnvironmentPlugin.execute(path: Path, directory: Path = plugin.paths.runtimeDirectory, script: String) {
    directory.touch().resolve(path).writeContent(script.trimIndent())
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    val scriptPath = path.toAbsolutePath()
    val processResult = ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(scriptPath.toString())
            .execute()
    project.run {
        attention("Script executed")
        attention("Directory: $directory")
        attention("Exit code: ${processResult.exitValue}")
        attention("Output: ${processResult.outputString()}")
        attention("Script $scriptPath", name)
    }
    logExecution(output, error)
}


fun EnvironmentPlugin.process(name: String, path: Path, directory: Path = plugin.paths.runtimeDirectory, script: () -> String) = process(name, path, directory, script())

fun EnvironmentPlugin.process(name: String, path: Path, directory: Path = plugin.paths.runtimeDirectory, script: String) {
    runCatching {
        processLogsWriters.remove(name)?.cancel(true)
        directory.resolve(name).stdout().toFile().delete()
        directory.resolve(name).stderr().toFile().delete()
    }
    directory.resolve(path).writeContent(script.trimIndent())
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    processLogsWriters[name] = localLogsScheduler.scheduleAtFixedRate(
            { name.logProcess(directory.resolve(name), output, error) },
            0,
            500L,
            MILLISECONDS
    )
    val scriptPath = path.toAbsolutePath().toString()
    ProcessExecutor()
            .directory(directory.toFile())
            .redirectOutputAlsoTo(output)
            .redirectErrorAlsoTo(error)
            .command(scriptPath)
            .start()
    project.run {
        attention("Process started", name)
        attention("Script $scriptPath", name)
        attention("Output - ${directory.resolve(name).stdout()}", name)
        attention("Error - ${directory.resolve(name).stderr()}", name)
    }
}

private fun String.logProcess(log: Path, output: ByteArrayOutputStream, error: ByteArrayOutputStream) {
    output.apply {
        toString()
                .lineSequence()
                .filter { line -> line.isNotBlank() }
                .forEach { line -> log.stdout().appendContent("(${this@logProcess}): $line$NEW_LINE") }
        reset()
    }
    error.apply {
        toString()
                .lineSequence()
                .filter { line -> line.isNotBlank() }
                .forEach { line -> log.stderr().appendContent("(${this@logProcess}): $line$NEW_LINE") }
        reset()
    }
}

private fun EnvironmentPlugin.logExecution(output: OutputStream, error: OutputStream) {
    output.toString()
            .lineSequence()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> project.attention(line) }
    error.toString()
            .lineSequence()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> project.error(line) }
}
