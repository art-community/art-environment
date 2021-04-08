/*
 * ART
 *
 * Copyright 2020 ART
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

package logger

import logger.LogMessageColor.*
import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.OutputStream

const val ANSI_RESET = "\u001B[0m"

enum class LogMessageColor(val code: String) {
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    BLACK_BOLD("\u001b[1;30m"),
    RED_BOLD("\u001b[1;31m"),
    GREEN_BOLD("\u001b[1;32m"),
    YELLOW_BOLD("\u001b[1;33m"),
    BLUE_BOLD("\u001b[1;34m"),
    PURPLE_BOLD("\u001b[1;35m"),
    CYAN_BOLD("\u001b[1;36m"),
    WHITE_BOLD("\u001b[1;37m")
}

fun message(message: String, color: LogMessageColor) = "${color.code}$message$ANSI_RESET"

fun Project.quiet(message: String, color: LogMessageColor = BLACK) {
    logger.quiet("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.success(message: String, color: LogMessageColor = GREEN_BOLD) {
    logger.quiet("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.warning(message: String, color: LogMessageColor = YELLOW_BOLD) {
    logger.quiet("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.attention(message: String, color: LogMessageColor = CYAN_BOLD) {
    logger.quiet("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.additionalAttention(message: String, color: LogMessageColor = PURPLE_BOLD) {
    logger.quiet("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.error(message: String, color: LogMessageColor = RED) {
    logger.error("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.info(message: String, color: LogMessageColor = BLACK) {
    logger.info("${name(color)} ${color.code}$message$ANSI_RESET")
}

fun Project.infoOutput() = object : OutputStream() {
    val buffer = ByteArrayOutputStream()

    override fun write(byte: Int) = buffer.write(byte)

    override fun flush() = info(buffer.toString())

    override fun close() = info(buffer.toString())
}

fun Project.debug(message: String, color: LogMessageColor = YELLOW) {
    logger.debug("${name(color)} ${color.code}$message$ANSI_RESET")
}

private fun Project.name(color: LogMessageColor) = message("[$name]:", color)
