package constants

enum class ExecutionMode {
    LOCAL,
    WSL,
    REMOTE
}

const val DEFAULT_USERNAME = "username"
const val DEFAULT_PASSWORD = "password"
const val LOG_FILE_REFRESH_PERIOD = 500L

const val STDOUT_LOG = ".stdout.log"
const val STDERR_LOG = ".stderr.log"

const val REDIRECT_STDIN = "<"
const val REDIRECT_STDOUT = "1>"
const val REDIRECT_STDERR = "2>"
const val SH = "sh"
const val ECHO = "echo"
const val NOHUP = "nohup"
const val DEV_NULL = "/dev/null"
const val SUDO = "sudo"
const val SUDO_SHELL_FLAG = "-S"
const val COMMAND_FLAG = "-c"
const val MKDIR_WITH_PARENTS = "mkdir -p"
const val COPY_RECURSIVE = "cp -r"
const val MOVE = "mv"
const val REMOVE_FORCE = "rm -rf"
const val JAVA_JAR = "java -jar"
const val CHOWN_RECURSIVE = "chown -R"
const val CHMOD_FILE = "chmod 677"
const val CHMOD_EXECUTABLE = "chmod 777"
const val CHMOD_DIRECTORY = "chmod 755"
const val KILL = "kill -9"
