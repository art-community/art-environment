package logger

import constants.EMPTY_STRING
import org.gradle.api.Project

class ContextLogger(private val context: String, private val project: Project) {
    fun log(message: String) = project.log(message, context)
    fun error(message: String) = project.error(message, context)
    fun error(error: Throwable) = project.error(error, context)
    fun info(message: String) = project.info(message, context)
    fun debug(message: String) = project.debug(message, context)
    fun line() = project.log(EMPTY_STRING)
}
