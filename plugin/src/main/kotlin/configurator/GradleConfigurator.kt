package configurator

import constants.GRADLE_PROPERTIES
import constants.GRADLE_PROPERTIES_TEMPLATE
import plugin.EnvironmentPlugin
import service.projectsDirectory
import service.writeText

fun EnvironmentPlugin.configureGradle() = projectsDirectory
        .resolve(GRADLE_PROPERTIES)
        .writeText(GRADLE_PROPERTIES_TEMPLATE)
