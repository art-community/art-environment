package configuration

import java.nio.file.Path
import javax.inject.Inject

open class LocalExecutionConfiguration @Inject constructor() {
    var executable: String? = null
        private set

    var directory: Path? = null
        private set

    fun executable(executable: String) {
        this.executable = executable
    }

    fun directory(directory: Path) {
        this.directory = directory
    }
}
