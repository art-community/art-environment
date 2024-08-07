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

package plugin

import configurator.configureTasks
import constants.ART
import configuration.EnvironmentConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.process.internal.shutdown.ShutdownHooks.addShutdownHook
import service.configureReleasing
import java.util.concurrent.ExecutorService

lateinit var plugin: EnvironmentPlugin
    private set

class EnvironmentPlugin : Plugin<Project> {
    lateinit var project: Project
        private set
    lateinit var configuration: EnvironmentConfiguration
        private set
    private val executors = mutableListOf<ExecutorService>()

    fun <T : ExecutorService> register(executorService: T): T = executorService.apply { executors += this }

    override fun apply(target: Project) {
        plugin = this
        project = target
        configuration = target.extensions.create(ART)
        target.runCatching {
            addShutdownHook { executors.forEach(ExecutorService::shutdownNow) }
            configureTasks()
            afterEvaluate { configureReleasing() }
        }.onFailure { error -> target.logger.error(error.message, error) }
    }
}
