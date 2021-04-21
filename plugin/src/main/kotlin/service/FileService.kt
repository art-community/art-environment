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

package service

import constants.*
import plugin.EnvironmentPlugin
import java.io.File
import java.io.IOException
import java.nio.charset.Charset.defaultCharset
import java.nio.file.Files
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.*
import java.util.*

val EnvironmentPlugin.environmentDirectory: Path
    get() = project.projectDir.toPath()

val EnvironmentPlugin.projectsDirectory
    get() = project.projectDir.resolve(PROJECTS).toPath().touchDirectory()

val EnvironmentPlugin.runtimeDirectory
    get() = project.projectDir.resolve(RUNTIME).toPath().touchDirectory()

fun EnvironmentPlugin.projectDirectory(project: String): Path =
        projectsDirectory.resolve(PROJECT_NAMES[project]!!)


fun Path.touchDirectory(): Path {
    if (toFile().exists()) return this
    if (!toFile().mkdirs()) {
        throw fileCreationException(parent)
    }
    return this
}

fun Path.touchFile(): Path {
    if (toFile().exists()) return this
    parent.touchDirectory()
    return writeText(EMPTY_STRING)
}

fun Path.writeText(text: String): Path {
    val asFile = parent.toFile()
    if (asFile.exists()) {
        if (!asFile.exists()) asFile.createNewFile()
        toFile().writeText(text, charset = defaultCharset())
        return this
    }
    if (!asFile.mkdirs()) {
        throw fileCreationException(parent)
    }
    if (!asFile.exists()) asFile.createNewFile()
    asFile.writeText(text, charset = defaultCharset())
    return this
}

fun Path.appendText(text: String): Path {
    val asFile = toFile()
    if (parent.toFile().exists()) {
        if (!asFile.exists()) asFile.createNewFile()
        asFile.appendText(text, charset = defaultCharset())
        return this
    }
    if (!parent.toFile().mkdirs()) {
        throw fileCreationException(parent)
    }
    if (!asFile.exists()) asFile.createNewFile()
    asFile.appendText(text, charset = defaultCharset())
    return this
}

fun Path.setExecutable() = apply { toFile().setExecutable(true) }

fun Path.clear() = toFile().takeIf(File::exists)?.writeText(EMPTY_STRING)

fun Path.copyTo(other: Path) {
    if (other.toFile().exists()) return
    copy(this, other, REPLACE_EXISTING, COPY_ATTRIBUTES)
}

fun Path.deleteRecursive(): Boolean = toFile().deleteRecursive()

fun File.deleteRecursive(): Boolean {
    if (!exists()) {
        return true
    }
    if (isFile) {
        return delete()
    }
    val children = listFiles()
    if (children.isNullOrEmpty()) {
        return delete()
    }
    children.forEach(File::deleteRecursive)
    return delete()
}

fun Path.copyRecursive(to: Path) {
    val file = toFile()
    if (!file.exists()) {
        return
    }
    try {
        if (file.isFile) {
            if (!to.parent.toFile().exists()) {
                createDirectories(to.parent)
            }
            copy(this, to, REPLACE_EXISTING)
            return
        }
        val children = file.listFiles()
        if (children.isNullOrEmpty()) {
            return
        }
        for (child in children) {
            val toPath = to.resolve(child.toPath().fileName)
            child.toPath().copyRecursive(toPath)
        }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
    }
}

fun Path.toWsl(): String {
    var path = this.toAbsolutePath().toString()
    if (!isWindows) {
        return toString()
    }
    if (path.isBlank()) {
        return toString()
    }
    if (SLASH == EMPTY_STRING + path[0] || BACKWARD_SLASH == EMPTY_STRING + path[0]) {
        path = path.substring(1)
    }
    if (path.contains(WINDOWS_DISK_PATH_SLASH) || path.contains(WINDOWS_DISK_PATH_BACKWARD_SLASH)) {
        path = path
                .replace(WINDOWS_DISK_PATH_SLASH.toRegex(), SLASH)
                .replace(WINDOWS_DISK_PATH_BACKWARD_SLASH_REGEX.toRegex(), SLASH)
                .replace(BACKWARD_SLASH_REGEX.toRegex(), SLASH)
        val firstLetter: String = EMPTY_STRING + path[0]
        return WSL_DISK_PREFIX + firstLetter.toLowerCase() + path.substring(1)
    }
    return toString()
}

fun Path.bat(): Path = Paths.get("${toAbsolutePath()}$DOT_BAT")

fun Path.sh(): Path = Paths.get("${toAbsolutePath()}$DOT_SH")

fun Path.lua(): Path = Paths.get("${toAbsolutePath()}$DOT_LUA")

fun Path.pid(): Path = Paths.get("${toAbsolutePath()}$DOT_PID")

fun Path.stdout(): Path = Paths.get("${toAbsolutePath()}$DOT_STDOUT")

fun Path.stderr(): Path = Paths.get("${toAbsolutePath()}$DOT_STDERR")

fun String.sh() = "$this$DOT_SH"

fun String.stdout() = "$this$DOT_STDOUT"

fun String.stderr() = "$this$DOT_STDERR"

fun String.lua() = "$this$DOT_LUA"

fun String.pid() = "$this$DOT_PID"

fun String.parent() = substringBeforeLast(SLASH)

fun String.resolve(path: String) = "$this/$path"

