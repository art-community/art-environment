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

const val WSL = "wsl"
const val E_ARGUMENT = "-e"
const val C_ARGUMENT = "-e"
const val PASS_ARGUMENTS = "--"
const val BASH = "bas"
const val KILL = "kill"
const val TERMINATE_SIGNAL = "-9"
const val MKDIR = "mkdir"
const val RM = "rm"
const val TEST = "test"
const val TOUCH = "touch"
const val CD = "cd"
const val NOHUP = "nohup"
const val P_ARGUMENT = "-p"
const val RF_ARGUMENT = "-rf"
const val D_ARGUMENT = "-d"
const val F_ARGUMENT = "-f"
const val STDOUT_PIPE = "1>"
const val STDERR_PIPE = "2>"
