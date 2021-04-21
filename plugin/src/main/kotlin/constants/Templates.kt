package constants

const val PROJECTS_NAME_TEMPLATE = """rootProject.name = "projects""""

val PROJECTS_GRADLE_BUILD_TEMPLATE =
        """
                    tasks.withType(type = Wrapper::class) {
                        gradleVersion = "7.0"
                    }
        """.trimIndent()

val GRADLE_PROPERTIES_TEMPLATE = """
        org.gradle.parallel=true
        org.gradle.jvmargs=-Xms1g -Xmx1g -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8 -Dhttps.protocols=TLSv1.2
        org.gradle.warning.mode=NONE
    """.trimIndent()

val INCLUDE_BUILD_TEMPLATE = { name: String -> """includeBuild("$name")""" }

val PUBLISHING_PROPERTIES_TEMPLATE = { username: String, password: String ->
    """
        publisher.username=$username
        publisher.password=$password
    """.trimIndent()
}

val LOG_TEMPLATE = { context: String, line: String -> "($context): $line" }

const val SANDBOX_SETTINGS_TEMPLATE = """rootProject.name = "sandbox""""
