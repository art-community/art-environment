import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

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

open class TarantoolConfiguration @Inject constructor(objectFactory: ObjectFactory) : ProjectConfiguration(objectFactory, TARANTOOL) {
    val instances = objectFactory.domainObjectContainer(InstanceConfiguration::class.java)

    fun instance(name: String, lua: () -> String = { "" }) {
        instances.register(name) { lua(lua()) }
    }

    open class InstanceConfiguration(val name: String) {
        var executable = determineCommand()
            private set
        lateinit var lua: String
            private set

        fun executable(executable: String) {
            this.executable = executable
        }

        fun lua(script: String) {
            this.lua = script
        }

        private fun determineCommand() = when {
            isWindows -> "cmd /k wsl -c tarantool"
            else -> "tarantool"
        }
    }
}
