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
import java.nio.file.Path


fun EnvironmentPlugin.stopLinuxProcess(name: String, directory: Path) {
    directory.resolve(name)
            .pid()
            .toFile()
            .takeIf { pid -> pid.exists() }
            ?.apply {
                readText().takeIf { pid -> pid.isNotBlank() }
                        ?.toInt()
                        ?.let { pid ->
                            execute("kill", "--", "-9", pid.toString())
                            project.attention("Linux: killed process $pid", name)
                        }
                delete()
            }

}


fun EnvironmentPlugin.restartLinuxProcess(name: String, directory: Path, script: () -> String) = process(name, directory.resolve(name).sh(), directory) {
    buildString {
        directory.resolve(name).pid().toFile().apply {
            takeIf { file -> file.exists() }
                    ?.readText()
                    ?.takeIf { string -> string.isNotBlank() }
                    ?.toInt()
                    ?.let { pid ->
                        appendLine("""kill -9 $pid""")
                        project.attention("Linux: killed process $pid", name)
                    }
            delete()
            createNewFile()
        }
        appendLine(script())
    }
}