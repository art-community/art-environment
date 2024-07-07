package configuration

import javax.inject.Inject

open class ReleaseConfiguration @Inject constructor() {
    var version: String? = null
        private  set
    var only: String? = null
        private  set

    fun only(project: String) {
        this.only = project
    }

    fun version(version: String) {
        this.version = version
    }
}