/*
 * ART
 *
 * Copyright 2019-2022 ART
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

import constants.*
import org.gradle.api.Project
import plugin.plugin
import service.cleanTarantool
import service.restartTarantool
import service.stopTarantool

fun Project.configureTasks() {
    tasks.register(CONFIGURE) {
        group = ART
        doLast {
            plugin.run {
                configureProjects()
                configurePublishing()
                configureGradle()
            }
        }
    }

    tasks.register(TARANTOOL_RESTART) {
        group = ART
        doLast { plugin.run { restartTarantool() } }
    }

    tasks.register(TARANTOOL_STOP) {
        group = ART
        doLast { plugin.run { stopTarantool() } }
    }

    tasks.register(TARANTOOL_CLEAN) {
        group = ART
        doLast { plugin.run { cleanTarantool() } }
    }
}
