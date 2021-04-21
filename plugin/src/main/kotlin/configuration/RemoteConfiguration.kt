package configuration

import constants.AuthenticationMode
import constants.AuthenticationMode.KEY
import constants.AuthenticationMode.PASSWORD
import constants.LOCALHOST
import constants.REMOTE_BASE_DIRECTORY
import java.nio.file.Path
import javax.inject.Inject

open class RemoteConfiguration @Inject constructor() {
    private var directory: String? = null

    var authenticationMode: AuthenticationMode = KEY
        private set

    var host: String = LOCALHOST
        private set

    var port: Int? = null
        private set

    lateinit var user: String
        private set

    lateinit var password: String
        private set

    var keyLocations: Set<Path> = setOf()
        private set

    fun host(host: String) {
        this.host = host
    }

    fun port(port: Int) {
        this.port = port
    }

    fun user(user: String) {
        this.user = user
    }

    fun password(password: String) {
        this.password = password
        authenticationMode = PASSWORD
    }

    fun credentials(user: String, password: String) {
        this.user = user
        this.password = password
        authenticationMode = PASSWORD
    }

    fun key(path: Path) {
        this.keyLocations = keyLocations + path
        authenticationMode = KEY
    }

    fun directory(directory: String) {
        this.directory = directory
    }

    fun directory() = directory ?: REMOTE_BASE_DIRECTORY(user)
}
