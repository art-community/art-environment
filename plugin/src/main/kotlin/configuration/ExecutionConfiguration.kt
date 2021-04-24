package configuration

import plugin.plugin
import service.runtimeDirectory
import service.touchDirectory
import java.nio.file.Path
import javax.inject.Inject

open class ExecutionConfiguration @Inject constructor() {
    var executable: String? = null
        private set

    private var localDirectory: Path? = null

    private var remoteDirectory: String? = null

    fun executable(executable: String) {
        this.executable = executable
    }

    fun localDirectory(directory: Path) {
        this.localDirectory = directory
    }

    fun remoteDirectory(directory: String) {
        this.remoteDirectory = directory
    }

    fun localDirectory() = localDirectory

    fun remoteDirectory() = remoteDirectory
}
