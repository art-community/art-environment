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

repositories {
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

tasks.withType(type = Wrapper::class) {
    gradleVersion = "7.0-rc-2"
}

gradlePlugin {
    plugins {
        register("environment") {
            id = "environment"
            implementationClass = "plugin.EnvironmentPlugin"
        }
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:+")
    implementation("org.zeroturnaround:zt-exec:+")
    implementation("com.hierynomus", "sshj", "+")
}