import org.gradle.api.Plugin
import org.gradle.api.Project
import org.zeroturnaround.exec.ProcessExecutor

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



class EnvironmentPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val art = extensions.create(ART, ArtExtension::class.java, this)

        tasks.register(CONFIGURE) {
            group = ART
        }

        tasks.register(BOOTSTRAP_TARANTOOL) {
            group = ART
            doLast {
                ProcessExecutor().command("bash", "-c", """"pkill tarantool"""").execute()
                art.tarantoolConfiguration.instances.forEach { instance -> runTarantool(instance.lua, instance.name) }
            }
        }

        Thread.currentThread().join()
    }
}





