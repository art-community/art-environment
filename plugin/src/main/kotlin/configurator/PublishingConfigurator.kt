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

package configurator

import constants.PROJECTS
import constants.PUBLISHING_PROPERTIES
import constants.PUBLISHING_PROPERTIES_TEMPLATE
import logger.attention
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.configurePublishing() = extension.publishingConfiguration.run {
    if (!enabled) {
        project.projectDir.resolve(PROJECTS).resolve(PUBLISHING_PROPERTIES).delete()
        return@run
    }
    project.projectDir.resolve(PROJECTS).resolve(PUBLISHING_PROPERTIES).apply {
        writeText(PUBLISHING_PROPERTIES_TEMPLATE(username, password))
        project.attention("Publishing activated. Properties: $absolutePath")
    }
}
