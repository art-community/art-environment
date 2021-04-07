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

package constants

import org.gradle.api.Action
import java.lang.System.getProperty

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
const val WINDOWS_DISK_PATH_SLASH = ":/"
const val WINDOWS_DISK_PATH_BACKWARD_SLASH = ":\\"
const val WINDOWS_DISK_PATH_BACKWARD_SLASH_REGEX = ":\\\\"
const val WSL_DISK_PREFIX = "/mnt/"
const val OS_NAME_PROPERTY = "os.name"
const val RUNTIME = "runtime"
const val SCRIPTS = "scripts"
const val LOCALHOST = "127.0.0.1"
const val STDIN = "<"
const val STDOUT = "1>"
const val STDERR = "2>"
const val SH = "sh"
const val ECHO = "echo"
const val NOHUP = "nohup"
const val DEV_NULL = "/dev/null"
const val SUDO = "sudo"
const val SUDO_SHELL_FLAG = "-S"
const val COMMAND_FLAG = "-c"
const val MKDIR_WITH_PARENTS = "mkdir -p"
const val COPY_RECURSIVE = "cp -r"
const val MOVE = "mv"
const val REMOVE_FORCE = "rm -rf"
const val JAVA_JAR = "java -jar"
const val CHOWN_RECURSIVE = "chown -R"
const val CHMOD_FILE = "chmod 677"
const val CHMOD_EXECUTABLE = "chmod 777"
const val CHMOD_DIRECTORY = "chmod 755"
const val KILL = "kill -9"
const val PID_OF = "pidof"
const val NOHUP_OUT = "nohup.out"
const val NOHUP_ERR = "nohup.err"
const val STDOUT_LOG = ".stdout.log"
const val STDERR_LOG = ".stderr.log"
const val DOT_BAT = ".bat"
const val DOT_SH = ".sh"
const val DOT_LUA = ".lua"


const val WIN = "win";
const val MAC = "mac";
const val NIX = "nix";
const val NUX = "nux";
const val AIX = "aix";
const val SUNOS = "sunos";

val OS = getProperty(OS_NAME_PROPERTY).toLowerCase()

val isWindows: Boolean
    get() = OS.contains(WIN)

val isMac: Boolean
    get() = OS.contains(MAC)

val isNix: Boolean
    get() = OS.contains(NIX)

val isNux: Boolean
    get() = OS.contains(NUX)

val isAix: Boolean
    get() = OS.contains(AIX)

val isSunos: Boolean
    get() = OS.contains(SUNOS)


val EMPTY_ACTION = Action<Any> { }

val PROJECT_SOURCES = mapOf(
        JAVA to "art-java",
        KOTLIN to "art-kotlin",
        TARANTOOL to "art-tarantool",
        GENERATOR to "art-generator"
)

val SUDO_LOG = Regex("\\[sudo]\\spassword\\sfor.+?:")
val REMOTE_RUNTIME_DIRECTORY = { project: String -> "~/art/$project/runtime" }
val REMOTE_SCRIPTS_DIRECTORY = { project: String -> "~/art/$project/scripts" }
