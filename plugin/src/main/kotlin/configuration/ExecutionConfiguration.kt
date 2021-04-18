package configuration

import constants.RUNTIME
import plugin.plugin
import service.touchDirectory
import java.nio.file.Path
import javax.inject.Inject

open class ExecutionConfiguration @Inject constructor() {
    var executable: String? = null
        private set

    private var directory: Path? = null

    fun executable(executable: String) {
        this.executable = executable
    }

    fun directory(directory: Path) {
        this.directory = directory
    }

    fun directory() = directory
            ?.touchDirectory()
            ?: plugin.project.projectDir.resolve(RUNTIME).toPath().touchDirectory()
}
