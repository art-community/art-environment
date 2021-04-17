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

package configuration

import constants.DEFAULT_TARANTOOL_PORT
import constants.EMPTY_STRING
import constants.TARANTOOL
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import plugin.plugin
import javax.inject.Inject

open class TarantoolConfiguration @Inject constructor(objectFactory: ObjectFactory, executableConfiguration: ExecutableConfiguration)
    : ProjectConfiguration(TARANTOOL), ExecutableConfiguration by executableConfiguration {
    val instances: NamedDomainObjectContainer<InstanceConfiguration> = objectFactory.domainObjectContainer(InstanceConfiguration::class.java)

    fun instance(name: String, configurator: Action<in InstanceConfiguration>) {
        instances.create(name, configurator)
    }

    open class InstanceConfiguration(val name: String) {
        var port: Int = DEFAULT_TARANTOOL_PORT
            private set
        var configuration: String = EMPTY_STRING
            private set
        var execution: String = EMPTY_STRING
            private set

        fun port(port: Int) {
            this.port = port
        }

        fun configure(script: () -> String) {
            this.configuration = script()
        }

        fun execute(script: () -> String) {
            this.execution = script()
        }

        fun execute(script: String) {
            this.execution = script
        }

        fun executeFile(script: String) {
            this.execution = plugin.project.projectDir.resolve(script).readText()
        }

        fun toLua() =
                """
                    box.cfg {
                        listen = $port,
                        pid_file = "${name}.pid",
                        $configuration
                    }
                    $execution
                """.trimIndent()

    }
}
