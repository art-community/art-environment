package configuration

import javax.inject.Inject

open class ReleaseConfiguration @Inject constructor() {
    var version: String? = null
        private  set

    fun version(version: String) {
        this.version = version
    }
}