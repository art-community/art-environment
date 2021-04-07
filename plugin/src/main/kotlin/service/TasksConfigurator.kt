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

package configurator

import constants.ART
import constants.CONFIGURE
import org.gradle.api.Project
import plugin.plugin
import service.configureProjects
import service.execute
import java.nio.file.Paths

fun Project.configureTasks() {
    tasks.register(CONFIGURE) {
        group = ART
        doLast { plugin.project.configureProjects() }
    }
    tasks.register("debug") {
        doLast {
            execute("cmd.exe", "/c", "echo", "test")
            execute(Paths.get("test"), "cmd.exe", "/c", "echo", "test")
            execute("test") {
                """
                   echo test 
                """
            }
            execute("test", Paths.get("test")) {
                """
                   echo test 
                """
            }
        }
    }
}
