package service

import constants.NEW_LINE
import logger.logger
import plugin.EnvironmentPlugin
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.file.Path

fun Path.localProcessLog(output: ByteArrayOutputStream, error: ByteArrayOutputStream) {
    output.apply {
        toString()
                .lineSequence()
                .filter { line -> line.isNotBlank() }
                .forEach { line -> stdout().appendText("$line$NEW_LINE") }
        reset()
    }
    error.apply {
        toString()
                .lineSequence()
                .filter { line -> line.isNotBlank() }
                .forEach { line -> stderr().appendText("$line$NEW_LINE") }
        reset()
    }
}

fun EnvironmentPlugin.consoleLog(output: OutputStream, error: OutputStream, context: String = project.name) {
    val logger = project.logger(context)
    output.toString()
            .lineSequence()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> logger.attention(line) }
    error.toString()
            .lineSequence()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> logger.error(line) }
}
