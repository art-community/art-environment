package logger

import org.gradle.api.Project

class ContextLogger(private val context: String, private val project: Project) {
    fun quiet(message: String) = project.quiet(message, context)
    fun success(message: String) = project.success(message, context)
    fun warning(message: String) = project.warning(message, context)
    fun attention(message: String) = project.attention(message, context)
    fun additional(message: String) = project.additional(message, context)
    fun error(message: String) = project.error(message, context)
    fun error(error: Throwable) = project.error(error, context)
    fun info(message: String) = project.info(message, context)
    fun debug(message: String) = project.debug(message, context)
}
