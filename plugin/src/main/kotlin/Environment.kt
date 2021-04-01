/*
 *
 *  * ART
 *  *
 *  * Copyright 2020 ART
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.GradleDsl

class Environment

class EnvironmentConfiguration {
    var communityUrl = "https://github.com/art-community/"
    val projects = mutableSetOf<String>()

    fun url(url: String) {
        communityUrl = url
    }

    fun includeJava() {
        projects += "art-java"
    }

    fun includeKotlin() {
        projects += "art-kotlin"
    }

    fun includeTarantool() {
        projects += "art-tarantool"
    }
}

fun Settings.art(configurator: EnvironmentConfiguration.() -> Unit) {
    success("Welcome to ART development environment")
    val environmentConfiguration = EnvironmentConfiguration()
    configurator(environmentConfiguration)
    //fetchProject(environmentConfiguration.communityUrl + "art-java", "java")
    include("projects:java")

    gradle.projectsLoaded {
        rootProject.tasks.register("activateTarantool") {
            group = "art"
            doLast {
                success("Tarantool activated")
            }
        }
    }
}
