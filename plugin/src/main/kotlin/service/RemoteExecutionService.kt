/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package service

import configuration.RemoteConfiguration
import constants.AuthenticationMode.KEY
import constants.AuthenticationMode.PASSWORD
import constants.EMPTY_STRING
import constants.RUNTIME
import constants.SSH_TIMEOUT
import logger.attention
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.OpenMode.READ
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FilePermission
import net.schmizz.sshj.xfer.FilePermission.*
import net.schmizz.sshj.xfer.FileSystemFile
import plugin.plugin
import java.io.ByteArrayOutputStream
import java.nio.file.Files.createTempFile
import java.nio.file.Path

data class RemoteClient(val ssh: SSHClient, val configuration: RemoteConfiguration)

fun <T> RemoteConfiguration.ssh(executor: RemoteClient.() -> T): T {
    SSHClient().use { client ->
        with(client) {
            addHostKeyVerifier(PromiscuousVerifier())
            addAlgorithmsVerifier { true }
            port?.let { sshPort -> connect(host, sshPort) } ?: connect(host)
            timeout = SSH_TIMEOUT
            when (authenticationMode) {
                PASSWORD -> authPassword(user, password)
                KEY -> when {
                    keyLocations.isNotEmpty() -> authPublickey(user, *keyLocations.map { location -> location.toAbsolutePath().toString() }.toTypedArray())
                    else -> authPublickey(user)
                }
            }
            return executor(RemoteClient(client, this@ssh))
        }
    }
}

class RemoteExecutionService(private var trace: Boolean, private var context: String, private val client: RemoteClient) {
    fun trace(trace: Boolean = true) {
        this.trace = trace
    }

    fun trace(trace: Boolean = true, context: String = this.context) {
        this.trace = trace
        this.context = context
    }

    fun context() = context

    fun <T> sftp(executor: SFTPClient.() -> T): T = with(client.ssh) { newSFTPClient().use { client -> executor(client) } }

    fun <T> session(executor: Session.() -> T) = with(client.ssh) { startSession().use { session -> executor(session) } }

    fun directoryExists(path: String) = execute("test -d $path").exitStatus == 0

    fun fileExists(path: String) = execute("test -f $path").exitStatus == 0

    fun kill(pid: Int) = execute("kill -9 $pid")

    fun delete(path: String) = execute("rm -rf $path")

    fun touchDirectory(path: String) = path.apply {
        if (directoryExists(path)) return@apply
        execute("mkdir -p $this")
    }

    fun touchFile(path: String) = path.apply { execute("touch $this") }

    fun runtimeDirectory() = touchDirectory("${plugin.extension.remoteConfiguration.directory()}/$RUNTIME")

    fun execute(command: String): Session.Command = session {
        val result = exec(command)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        output.writeBytes(result.inputStream.readBytes())
        error.writeBytes(result.errorStream.readBytes())
        if (!trace) return@session result
        plugin.printToConsole(output, error, context)
        plugin.project.run {
            attention("Remote command executed - $command", context)
            result.exitSignal?.let { signal -> attention("Exit signal - $signal", context) }
            attention("Exit status - ${result.exitStatus}", context)
            result.exitErrorMessage
                    ?.takeIf { message -> message.isNotBlank() }
                    ?.let { message -> attention("Exit message - $message", context) }
        }
        return@session result
    }

    fun sh(name: String, directory: String = runtimeDirectory(), script: () -> String) = sh(name, directory, script())

    fun sh(name: String, directory: String, script: String) = session {
        val path = directory.resolve(name)
        val scriptPath = path.sh()
        val stdoutPath = path.stdout()
        val stderrPath = path.stderr()
        writeExecutable(scriptPath, script)
        val command = """cd $directory && nohup bash $scriptPath 1>$stdoutPath 2>$stdoutPath &""".trimIndent()
        execute(command)
        plugin.project.run {
            attention("Remote process started", context)
            attention("Script - $scriptPath", context)
            attention("Output - $stdoutPath", context)
            attention("Error - $stderrPath", context)
        }
    }

    fun upload(localPath: Path, remotePath: String) = sftp { put(localPath.toAbsolutePath().toString(), remotePath) }
    fun download(remotePath: String, localPath: Path) = sftp { get(remotePath, localPath.apply { parent.touchDirectory() }.toString()) }

    fun readFile(remotePath: String): String = sftp {
        open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readText()
    }

    fun writeExecutable(remotePath: String, content: String): String =
            writeFile(remotePath, content, GRP_RWX, USR_RWX)

    fun writeFile(remotePath: String, content: String): String =
            writeFile(remotePath, content, USR_R, USR_W, GRP_R, GRP_W)

    fun writeFile(remotePath: String, content: String, vararg permissions: FilePermission): String = sftp {
        val attributes = FileAttributes.Builder().withPermissions(setOf(*permissions)).build()
        touchDirectory(remotePath.parent())
        createTempFile(EMPTY_STRING, EMPTY_STRING).apply {
            writeText(content)
            put(FileSystemFile(toFile()), remotePath)
            setattr(remotePath, attributes)
            toFile().delete()
        }
        remotePath
    }
}

fun <T> RemoteClient.remote(trace: Boolean = false, context: String = EMPTY_STRING, service: RemoteExecutionService.() -> T): T = with(configuration) {
    val remoteContext = when {
        context.isNotBlank() -> "[$context]: $user@$host${port?.let { port -> ":$port" } ?: ""}"
        else -> "$user@$host${port?.let { port -> ":$port" } ?: ""}"
    }
    return service(RemoteExecutionService(trace, remoteContext, this@remote))
}
