package constants

enum class ExecutionMode {
    LOCAL_EXECUTION,
    WSL_EXECUTION,
    REMOTE_EXECUTION
}

const val DEFAULT_USERNAME = "username"
const val DEFAULT_PASSWORD = "password"
const val LOG_FILE_REFRESH_PERIOD = 500L
