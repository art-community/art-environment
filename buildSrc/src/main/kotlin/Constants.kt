import org.gradle.api.Action
import java.lang.System.getProperty

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
const val PROJECTS = "projects"
const val DEFAULT_URL = "https://github.com/art-community"
const val SLASH = "/"
const val BACKWARD_SLASH = "\\"
const val BACKWARD_SLASH_REGEX = "\\\\"
const val EMPTY_STRING = ""
const val IP_ADDRESS_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
const val WINDOWS_DISK_PATH_SLASH = ":/"
const val WINDOWS_DISK_PATH_BACKWARD_SLASH = ":\\"
const val WINDOWS_DISK_PATH_BACKWARD_SLASH_REGEX = ":\\\\"
const val WSL_DISK_PREFIX = "/mnt/"
var OS_NAME_PROPERTY = "os.name"
var WIN = "win"
var MAC = "mac"
val EMPTY_ACTION = Action<Any> { }
val OS = getProperty(OS_NAME_PROPERTY).toLowerCase()
val isWindows: Boolean
    get() = OS.contains(WIN)

val PROJECT_SOURCES = mapOf(
        JAVA to "art-java",
        KOTLIN to "art-kotlin",
        TARANTOOL to "art-tarantool",
        GENERATOR to "art-generator"
)
