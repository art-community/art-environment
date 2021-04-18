package constants

import java.lang.System.getProperty


const val OS_NAME_PROPERTY = "os.name"
val OS = getProperty(OS_NAME_PROPERTY).toLowerCase()
const val WIN = "win"
const val MAC = "mac"
const val NIX = "nix"
const val NUX = "nux"
const val AIX = "aix"
const val SUNOS = "sunos"
const val WSL_DISK_PREFIX = "/mnt/"

val isWindows: Boolean
    get() = OS.contains(WIN)

val isMac: Boolean
    get() = OS.contains(MAC)

val isNix: Boolean
    get() = OS.contains(NIX)

val isNux: Boolean
    get() = OS.contains(NUX)

val isAix: Boolean
    get() = OS.contains(AIX)

val isSunos: Boolean
    get() = OS.contains(SUNOS)
