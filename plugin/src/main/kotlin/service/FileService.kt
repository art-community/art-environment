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

package service

import constants.*
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

fun Path.touch(): Path {
    if (toFile().exists()) return this
    if (!toFile().mkdirs()) {
        throw fileCreationException(parent)
    }
    return this
}

fun Path.write(content: String): Path {
    if (parent.toFile().exists()) {
        toFile().writeText(content, charset = Charset.defaultCharset())
        return this
    }
    if (!parent.toFile().mkdirs()) {
        throw fileCreationException(parent)
    }
    toFile().writeText(content, charset = Charset.defaultCharset())
    return this
}

fun Path.toWsl(): Path {
    var path = this.toAbsolutePath().toString()
    if (!isWindows) {
        return this
    }
    if (path.isBlank()) {
        return this
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
        return Paths.get(WSL_DISK_PREFIX + firstLetter.toLowerCase() + path.substring(1))
    }
    return this
}
