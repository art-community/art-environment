package constants

const val PROJECTS_NAME_TEMPLATE = """rootProject.name = "projects""""
val PROJECTS_GRADLE_BUILD_TEMPLATE =
        """
                    tasks.withType(type = Wrapper::class) {
                        gradleVersion = "7.0"
                    }
        """.trimIndent()
val INCLUDE_BUILD_TEMPLATE = { name: String -> """includeBuild("$name")""" }

val LOG_TEMPLATE = { context: String, line: String -> "($context): $line" }
