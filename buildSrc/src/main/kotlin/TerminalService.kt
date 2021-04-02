import org.zeroturnaround.exec.ProcessExecutor
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


fun openWindowsTerminal(command: String, label: String) = arrayOf(
        "cmd.exe",
        "/c",
        """start "$label" "$command""""
)

fun openMacTerminal(command: String) = arrayOf(
        "osascript",
        "-e",
        """'tell app "Terminal" to do script "$command"'"""
)

fun runMacScript(script: String, directory: Path, vararg arguments: String) = ProcessExecutor()
        .command(*(openMacTerminal(script) + arguments))
        .redirectOutput(System.out)
        .redirectError(System.err)
        .start()
        .process
