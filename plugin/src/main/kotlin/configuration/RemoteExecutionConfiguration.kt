package configuration

import constants.LOCALHOST
import java.nio.file.Path
import javax.inject.Inject

open class RemoteExecutionConfiguration @Inject constructor() {
    lateinit var executable: String
        private set

    lateinit var workingDirectory: Path
        private set

    var host: String = LOCALHOST
        private set

    var port: Int? = null
        private set

    var user: String? = null
        private set

    var password: String? = null
        private set

    fun host(host: String) {
        this.host = host
    }

    fun port(port: Int) {
        this.port = port
    }

    fun credentials(user: String, password: String) {
        this.user = user
        this.password = password
    }

    fun executable(executable: String) {
        this.executable = executable
    }

    fun directory(directory: Path) {
        this.workingDirectory = directory
    }
}
