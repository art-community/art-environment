package configuration

import constants.LOCALHOST
import javax.inject.Inject

open class SshConfiguration @Inject constructor() {
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
}
