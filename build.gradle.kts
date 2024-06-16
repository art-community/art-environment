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

tasks.withType(type = Wrapper::class) {
    gradleVersion = "8.5"
}

tasks.create("prepareLocalEnvironment") {
    doLast {
        if (file("local").exists()) return@doLast
        copy {
            from("global")
            into("local")
            include("global.gradle.kts")
            rename("global.gradle.kts", "local.gradle.kts")
        }
    }
}
