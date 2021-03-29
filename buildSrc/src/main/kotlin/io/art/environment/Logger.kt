package io.art.environment

import io.art.environment.AnsiColor.*
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

enum class AnsiColor(val code: String) {
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
    WHITE_BOLD("\u001b[1;37m");
}

private const val ANSI_RESET = "\u001B[0m"

val logger: Logger = Logging.getLogger(Environment::class.java)

fun message(message: String, color: AnsiColor): Unit = logger.quiet("${color.code}$message$ANSI_RESET")
fun success(message: String): Unit = message(message, GREEN)
fun error(message: String): Unit = message(message, RED)
fun warning(message: String): Unit = message(message, YELLOW)
fun additional(message: String): Unit = message(message, PURPLE_BOLD)
