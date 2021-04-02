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

fun convertToWslPath(windowsPath: String): String {
    var path = windowsPath
    if (!isWindows) {
        return path
    }
    if (path.isBlank()) {
        return path
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
    return path
}
