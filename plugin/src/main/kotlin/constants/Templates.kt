package constants

const val PROJECTS_NAME_TEMPLATE = """rootProject.name = "projects""""
val GRADLE_TASK_TEMPLATE =
        """
                    tasks.withType(type = Wrapper::class) {
                        gradleVersion = "7.0-rc-2"
                    }
        """.trimIndent()
val INCLUDE_BUILD_TEMPLATE = { name: String -> """includeBuild("$name")""" }

val LOG_TEMPLATE = { context: String, line: String -> "($context): $line" }
