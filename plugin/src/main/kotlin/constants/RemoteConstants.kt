package constants

import java.time.Duration.ofSeconds

enum class AuthenticationMode {
    PASSWORD,
    KEY
}

const val LOCALHOST = "127.0.0.1"

val SSH_TIMEOUT = ofSeconds(30).toMillis().toInt()
val REMOTE_BASE_DIRECTORY = { user: String -> "/home/$user/art" }
