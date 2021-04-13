package configurator

import constants.GRADLE_PROPERTIES
import constants.PROJECTS
import plugin.EnvironmentPlugin

fun EnvironmentPlugin.configureGradle() {
    val gradleProperties = """
        org.gradle.parallel=true
        org.gradle.jvmargs=-Xms1g -Xmx1g -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8 -Dhttps.protocols=TLSv1.2
        org.gradle.warning.mode=NONE
    """.trimIndent()
    project.projectDir
            .resolve(PROJECTS)
            .resolve(GRADLE_PROPERTIES)
            .writeText(gradleProperties)
}
