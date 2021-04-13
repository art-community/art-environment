package configurator

import constants.GRADLE_PROPERTIES
import constants.GRADLE_PROPERTIES_TEMPLATE
import constants.PROJECTS
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.configureGradle() = project.projectDir
        .resolve(PROJECTS)
        .resolve(GRADLE_PROPERTIES)
        .writeText(GRADLE_PROPERTIES_TEMPLATE)
