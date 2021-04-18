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

import constants.*
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import plugin.plugin
import javax.inject.Inject

open class TarantoolConfiguration @Inject constructor(objectFactory: ObjectFactory) : ProjectConfiguration(TARANTOOL) {
    val instances: NamedDomainObjectContainer<InstanceConfiguration> = objectFactory.domainObjectContainer(InstanceConfiguration::class.java)
    val executionConfiguration: ExecutionConfiguration = objectFactory.newInstance()
    lateinit var executionMode: ExecutionMode
        private set

    fun wsl(configurator: Action<in ExecutionConfiguration> = EMPTY_ACTION) {
        if (!isWindows) throw wslNotAvailableException()
        configurator.execute(executionConfiguration)
        executionMode = ExecutionMode.WSL
    }

    fun local(configurator: Action<in ExecutionConfiguration>) {
        if (isWindows) throw localNotAvailableException("Tarantool is not supported on Windows. ")
        configurator.execute(executionConfiguration)
        executionMode = ExecutionMode.LOCAL
    }

    fun remote(configurator: Action<in ExecutionConfiguration>) {
        configurator.execute(executionConfiguration)
        executionMode = ExecutionMode.REMOTE
    }

    fun instance(name: String, configurator: Action<in InstanceConfiguration>) {
        instances.create(name, configurator)
    }

    open class InstanceConfiguration(val name: String) {
        var port: Int = DEFAULT_TARANTOOL_PORT
            private set
        var configurationParameters: MutableMap<String, String> = mutableMapOf()
            private set
        var executionScript: String = EMPTY_STRING
            private set
        var includeModule: Boolean = true
            private set

        fun port(port: Int) {
            this.port = port
        }

        fun configure(name: String, value: String) {
            this.configurationParameters[name] = value
        }

        fun execute(script: () -> String) {
            this.executionScript = script()
        }

        fun execute(script: String) {
            this.executionScript = script
        }

        fun script(script: String) {
            this.executionScript = plugin.project.projectDir.resolve(script).readText()
        }

        fun excludeModule() {
            includeModule = false
        }

        fun toLua() = """
box.cfg {
    listen = $port,
    pid_file = "${name}.pid",
    ${configurationParameters.entries.joinToString { entry -> "${entry.key} = ${entry.value}," }}
}
box.schema.user.create('$DEFAULT_USERNAME', {password = '$DEFAULT_PASSWORD', if_not_exists = true})
box.schema.user.grant('$DEFAULT_USERNAME', 'read,write,execute,create,alter,drop', 'universe', nil, {if_not_exists=true})
${executionScript.trimIndent()}

""".trimIndent()

    }
}
