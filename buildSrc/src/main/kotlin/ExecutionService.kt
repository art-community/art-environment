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

import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import java.nio.file.Path

interface ExecutionService {
    fun execute(name: String, content: String, configurator: ExecutionConfiguration.() -> ExecutionConfiguration = { this })

    fun kill(process: ScriptProcess)

    fun runProcess(directory: Path, decorator: ProcessExecutor.() -> ProcessExecutor = { this }) =
            ProcessExecutor()
                    .directory(directory.toFile())
                    .let(decorator)
                    .start()
                    .registerProcess()

    fun executeProcess(directory: Path, decorator: ProcessExecutor.() -> ProcessExecutor = { this }): ProcessResult =
            ProcessExecutor()
                    .directory(directory.toFile())
                    .let(decorator)
                    .execute()
}

class ExecutionConfiguration {
    var directory: Path = plugin.paths.runtimeDirectory
        private set

    fun directory(directory: Path) = apply { this.directory = directory }
}

data class ScriptProcess(val process: StartedProcess,
                         val scriptName: String,
                         val scriptPath: Path, val scriptHash: String)

val executionService: ExecutionService
    get() = when {
        isWindows -> WindowsExecutionService
        else -> error("")
    }
