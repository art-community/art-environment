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
import constants.ExecutionMode.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

interface ExecutableConfiguration {
    fun wsl(configurator: Action<in LocalExecutionConfiguration> = EMPTY_ACTION)
    fun native(configurator: Action<in LocalExecutionConfiguration> = EMPTY_ACTION)
    fun remote(configurator: Action<in RemoteExecutionConfiguration> = EMPTY_ACTION)
    val localExecutionConfiguration: LocalExecutionConfiguration
    val remoteExecutionConfiguration: RemoteExecutionConfiguration
    val executionMode: ExecutionMode
}

open class ExecutableConfigurationImplementation @Inject constructor(objectFactory: ObjectFactory) : ExecutableConfiguration {
    override val localExecutionConfiguration: LocalExecutionConfiguration = objectFactory.newInstance()
    override val remoteExecutionConfiguration: RemoteExecutionConfiguration = objectFactory.newInstance()
    override lateinit var executionMode: ExecutionMode


    override fun wsl(configurator: Action<in LocalExecutionConfiguration>) {
        if (!isWindows) throw wslNotAvailableException()
        configurator.execute(localExecutionConfiguration)
        executionMode = WSL
    }

    override fun native(configurator: Action<in LocalExecutionConfiguration>) {
        if (isWindows) throw nativeNotAvailableException("Tarantool is not supported on Windows. ")
        configurator.execute(localExecutionConfiguration)
        executionMode = NATIVE
    }

    override fun remote(configurator: Action<in RemoteExecutionConfiguration>) {
        configurator.execute(remoteExecutionConfiguration)
        executionMode = REMOTE
    }
}
