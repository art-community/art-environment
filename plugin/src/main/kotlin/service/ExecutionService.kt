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
import logger.error
import org.gradle.process.internal.shutdown.ShutdownHooks.addShutdownHook
import org.zeroturnaround.exec.ProcessExecutor
import plugin.EnvironmentPlugin
import plugin.plugin
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.TimeUnit.SECONDS

private var executor = newSingleThreadScheduledExecutor()

fun EnvironmentPlugin.execute(vararg command: String) = execute(plugin.paths.runtimeDirectory, *command)

fun EnvironmentPlugin.execute(directory: Path, vararg command: String) {
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(*command)
            .execute()
    log(output, error)
}


fun EnvironmentPlugin.execute(path: Path, script: () -> String) = execute(path, plugin.paths.runtimeDirectory, script)

fun EnvironmentPlugin.execute(path: Path, script: String) = execute(path, plugin.paths.runtimeDirectory, script)


fun EnvironmentPlugin.execute(path: Path, directory: Path = plugin.paths.runtimeDirectory, script: () -> String) = execute(path, directory, script())

fun EnvironmentPlugin.execute(path: Path, directory: Path = plugin.paths.runtimeDirectory, script: String) {
    directory.touch().resolve(path).writeContent(script.trimIndent())
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(path.toAbsolutePath().toString())
            .start()
    log(output, error)
}


fun EnvironmentPlugin.process(name: String, path: Path, directory: Path = plugin.paths.runtimeDirectory, script: () -> String) {
    runCatching {
        executor.shutdownNow()
        executor = newSingleThreadScheduledExecutor()
    }
    directory.touch().resolve(path).writeContent(script().trimIndent())
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    executor.scheduleAtFixedRate({
        val log = directory.resolve(name).log()

        log.appendContent(output.toString())
        output.reset()

        log.appendContent(error.toString())
        error.reset()
    }, 0, 1L, SECONDS)
    addShutdownHook { runCatching { executor.shutdownNow() } }
    ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutputAlsoTo(output)
            .redirectErrorAlsoTo(error)
            .command(path.toAbsolutePath().toString())
            .start()
}

private fun EnvironmentPlugin.log(output: OutputStream, error: OutputStream) {
    output.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> project.attention(line) }
    error.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> project.error(line) }
}
