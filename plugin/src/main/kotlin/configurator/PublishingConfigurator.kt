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

package configurator

import constants.PUBLISHING_PROPERTIES
import constants.PUBLISHING_PROPERTIES_TEMPLATE
import logger.log
import plugin.EnvironmentPlugin
import service.projectsDirectory
import service.writeText

fun EnvironmentPlugin.configurePublishing() = configuration.publishingConfiguration.run {
    if (!enabled) {
        projectsDirectory.resolve(PUBLISHING_PROPERTIES).toFile().delete()
        return@run
    }
    projectsDirectory.resolve(PUBLISHING_PROPERTIES).apply {
        writeText(PUBLISHING_PROPERTIES_TEMPLATE(username, password))
        project.log("Publishing activated. Properties: ${toAbsolutePath()}")
    }
}
