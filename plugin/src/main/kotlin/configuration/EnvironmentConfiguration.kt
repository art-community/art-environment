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

package configuration

import constants.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

open class EnvironmentConfiguration @Inject constructor(objectFactory: ObjectFactory) {
    val projects = mutableSetOf<String>()

    var defaultUrl: String = DEFAULT_URL
        private set

    val javaConfiguration: ProjectConfiguration = objectFactory.newInstance(JAVA)

    val kotlinConfiguration: ProjectConfiguration = objectFactory.newInstance(KOTLIN)

    val generatorConfiguration: ProjectConfiguration = objectFactory.newInstance(GENERATOR)

    val uiConfiguration: ProjectConfiguration = objectFactory.newInstance(UI)

    val exampleConfiguration: ProjectConfiguration = objectFactory.newInstance(EXAMPLE)

    val fibersConfiguration: ProjectConfiguration = objectFactory.newInstance(FIBERS)

    val gradlePluginConfiguration: ProjectConfiguration = objectFactory.newInstance(GRADLE_PLUGIN)

    val tarantoolConfiguration: TarantoolConfiguration = objectFactory.newInstance()

    val publishingConfiguration: PublishingConfiguration = objectFactory.newInstance()

    val remoteConfiguration: RemoteConfiguration = objectFactory.newInstance()

    fun url(url: String) {
        defaultUrl = url
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

    fun gradlePlugin(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += GRADLE_PLUGIN
        configurator.execute(gradlePluginConfiguration)
    }

    fun example(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += EXAMPLE
        configurator.execute(exampleConfiguration)
    }

    fun ui(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += UI
        configurator.execute(uiConfiguration)
    }

    fun fibers(configurator: Action<in ProjectConfiguration> = EMPTY_ACTION) {
        projects += FIBERS
        configurator.execute(fibersConfiguration)
    }

    fun tarantool(configurator: Action<in TarantoolConfiguration> = EMPTY_ACTION) {
        projects += TARANTOOL
        configurator.execute(tarantoolConfiguration)
    }

    fun publishing(configurator: Action<in PublishingConfiguration>) {
        configurator.execute(publishingConfiguration)
    }

    fun remote(configurator: Action<in RemoteConfiguration> = EMPTY_ACTION) {
        configurator.execute(remoteConfiguration)
    }

    fun sandbox() {
        projects += SANDBOX
    }
}
