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

package service.windows

import org.zeroturnaround.exec.StartedProcess
import service.common.ExecutionConfiguration
import service.common.ExecutionService
import service.common.writeScript

object WindowsExecutionService : ExecutionService {
    override fun execute(name: String, content: String, configurator: ExecutionConfiguration.() -> ExecutionConfiguration) {
        runProcess(configurator(ExecutionConfiguration()).directory) {
            command("cmd.exe", "/c", """start "$name" "cmd /c ${writeScript(name, content).toAbsolutePath()}" """)
        }
    }

    override fun kill(process: StartedProcess) {
        process.process.destroy()
//        executeProcess(plugin.paths.runtimeDirectory) {
//            //command("taskkill", "/f", "/t", )
//        }
    }
}
