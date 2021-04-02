import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
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


const val ART = "art"
const val CONFIGURE = "configure"
const val BOOTSTRAP_TARANTOOL = "bootstrap-tarantool"
const val JAVA = "java"
const val KOTLIN = "kotlin"
const val TARANTOOL = "tarantool"
const val GENERATOR = "generator"

class EnvironmentPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val art = extensions.create(ART, ArtExtension::class.java, this)
        tasks.register(CONFIGURE) {
            group = ART
        }
        tasks.register(BOOTSTRAP_TARANTOOL) {
            group = ART
        }
        afterEvaluate {
        }
    }
}


open class ArtExtension @Inject constructor(objectFactory: ObjectFactory, val project: Project) {
    val projects = mutableSetOf<String>()
    val tarantoolConfiguration = objectFactory.newInstance(TarantoolConfiguration::class.java)

    fun java() {
        projects += JAVA
    }

    fun kotlin() {
        projects += KOTLIN
    }

    fun generator() {
        projects += GENERATOR
    }

    fun tarantool(configurator: Action<in TarantoolConfiguration>) {
        projects += TARANTOOL
        configurator.execute(tarantoolConfiguration)
    }
}

open class TarantoolConfiguration @Inject constructor(objectFactory: ObjectFactory) {
    val instances = objectFactory.domainObjectContainer(InstanceConfiguration::class.java)

    fun instance(name: String, lua: () -> String) {
        instances.register(name) { lua(lua()) }
    }

    open class InstanceConfiguration(val name: String) {
        lateinit var lua: String
            private set

        fun lua(script: String) {
            this.lua = script
        }
    }
}
