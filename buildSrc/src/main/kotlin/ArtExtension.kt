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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class ArtExtension @Inject constructor(objectFactory: ObjectFactory, val project: Project) {
    val projects = mutableSetOf<String>()

    var defaultUrl: String = DEFAULT_URL
        private set

    val javaConfiguration = objectFactory.newInstance(ProjectConfiguration::class.java, JAVA)

    val kotlinConfiguration = objectFactory.newInstance(ProjectConfiguration::class.java, KOTLIN)

    val generatorConfiguration = objectFactory.newInstance(ProjectConfiguration::class.java, GENERATOR)

    val tarantoolConfiguration = objectFactory.newInstance(TarantoolConfiguration::class.java)

    val sshConfiguration = objectFactory.newInstance(SshConfiguration::class.java)

    fun url(url: String) {
        this.defaultUrl = url
    }

    fun java(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += JAVA
        configurator.execute(javaConfiguration)
    }

    fun kotlin(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += KOTLIN
        configurator.execute(kotlinConfiguration)
    }

    fun generator(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += GENERATOR
        configurator.execute(generatorConfiguration)
    }

    fun tarantool(configurator: Action<in TarantoolConfiguration>) {
        projects += TARANTOOL
        configurator.execute(tarantoolConfiguration)
    }

    fun ssh(configurator: Action<in SshConfiguration>) {
        configurator.execute(sshConfiguration)
    }
}
