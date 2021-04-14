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

import constants.DOT_BAT
import constants.DOT_LUA
import constants.DOT_SH
import java.nio.file.Path
import java.nio.file.Paths


fun writeScript(path: Path, content: String) = path.writeContent(content)

fun Path.bat(): Path = Paths.get("${toAbsolutePath()}$DOT_BAT")

fun Path.sh(): Path = Paths.get("${toAbsolutePath()}$DOT_SH")

fun Path.lua(): Path = Paths.get("${toAbsolutePath()}$DOT_LUA")
