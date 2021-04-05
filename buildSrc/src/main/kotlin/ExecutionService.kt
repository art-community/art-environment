import org.gradle.api.Project
import org.zeroturnaround.exec.ProcessExecutor
import java.io.ByteArrayOutputStream
import java.nio.file.Path

/*
 * ART
 *
 * Copyright 2020 ART
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

fun Project.execute(vararg command: String) = execute(plugin.paths.runtimeDirectory, *command)

fun Project.execute(directory: Path, vararg command: String) {
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(*command)
            .execute()
    output.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> attention(line) }
    error.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> error(line) }
}


fun Project.execute(name: String, script: () -> String) = execute(name, plugin.paths.runtimeDirectory, script)

fun Project.execute(name: String, script: String) = execute(name, plugin.paths.runtimeDirectory, script)


fun Project.execute(name: String, directory: Path = plugin.paths.runtimeDirectory, script: () -> String) = execute(name, directory, script())

fun Project.execute(name: String, directory: Path = plugin.paths.runtimeDirectory, script: String) {
    val path = writeScript(plugin.paths.scriptsDirectory.touch().resolve(name).bat(), script.trimIndent())
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    ProcessExecutor()
            .directory(directory.touch().toFile())
            .redirectOutput(output)
            .redirectError(error)
            .command(path.toAbsolutePath().toString())
            .execute()
    output.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> attention(line) }
    error.toString().lineSequence().filter { line -> line.isNotBlank() }.forEach { line -> kotlin.error(line) }
}
