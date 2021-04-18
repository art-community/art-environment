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
import constants.PROJECTS
import constants.RUNTIME
import constants.SSH_TIMEOUT
import logger.attention
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.OpenMode.*
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FilePermission
import net.schmizz.sshj.xfer.FilePermission.*
import plugin.plugin
import java.io.ByteArrayOutputStream
import java.nio.file.Files.createDirectories
import java.nio.file.Path


data class RemoteClient(val configuration: RemoteConfiguration, val sshClient: SSHClient)

fun <T> RemoteConfiguration.ssh(executor: RemoteClient.() -> T): T {
    SSHClient().use { client ->
        with(client) {
            addHostKeyVerifier(PromiscuousVerifier())
            addAlgorithmsVerifier { true }
            port?.let { sshPort -> connect(host, sshPort) } ?: connect(host)
            timeout = SSH_TIMEOUT
            when (authenticationMode) {
                PASSWORD -> authPassword(user, password)
                KEY -> authPublickey(user)
            }
            return executor(RemoteClient(this@ssh, client))
        }
    }
}

fun <T> RemoteClient.sftp(executor: SFTPClient.() -> T): T = with(sshClient) { newSFTPClient().use { client -> executor(client) } }

fun RemoteClient.session(executor: Session.() -> Unit) = with(sshClient) { startSession().use { session -> executor(session) } }

fun RemoteClient.touchDirectory(path: String) = path.apply { execute(plugin.project.name, "mkdir -p $this") }

fun RemoteClient.touchFile(path: String) = path.apply { execute(plugin.project.name, "touch $this") }

fun RemoteClient.runtimeDirectory() = touchDirectory("${configuration.directory()}/$RUNTIME")

fun RemoteClient.projectsDirectory() = touchDirectory("${configuration.directory()}/$PROJECTS")

fun RemoteClient.projectDirectory(name: String) = touchDirectory("${configuration.directory()}/$PROJECTS/$name")

fun RemoteClient.execute(logContext: String, command: String) = session {
    val result = exec(command)
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    output.writeBytes(result.inputStream.readBytes())
    error.writeBytes(result.errorStream.readBytes())
    val context = "[$logContext]: ${configuration.user}@${configuration.host}${configuration.port?.let { port -> ":$port" } ?: ""}"
    plugin.project.run {
        attention("Remote command executed - $command", context)
        result.exitSignal?.let { signal -> attention("Exit signal - $signal", context) }
        attention("Exit status - ${result.exitStatus}", context)
        result.exitErrorMessage
                ?.takeIf { message -> message.isNotBlank() }
                ?.let { message -> attention("Exit message - $message", context) }
    }
    plugin.consoleLog(output, error, context)
}

fun RemoteClient.execute(context: String, directory: String, command: String) = execute(context, "cd $directory && $command")

fun RemoteClient.sh(name: String, directory: String = runtimeDirectory(), script: () -> String) = sh(name, directory, script())
fun RemoteClient.sh(name: String, directory: String, script: String) = session {
    val path = directory.resolve(name)
    val scriptPath = path.sh()
    val stdoutPath = path.stdout()
    val stderrPath = path.stderr()
    writeExecutable(scriptPath, script)
    val command = """cd $directory && nohup bash $scriptPath 1>$stdoutPath 2>$stdoutPath &""".trimIndent()
    execute(name, command)
    val context = "[$name]: ${configuration.user}@${configuration.host}${configuration.port?.let { port -> ":$port" } ?: ""}"
    plugin.project.run {
        attention("Remote process started", context)
        attention("Script - $scriptPath", context)
        attention("Output - $stdoutPath", context)
        attention("Error - $stderrPath", context)
    }
}

fun RemoteClient.upload(localPath: Path, remotePath: String) = sftp { put(localPath.toAbsolutePath().toString(), remotePath) }
fun RemoteClient.download(remotePath: String, localPath: Path) = sftp { get(remotePath, createDirectories(localPath).toString()) }

fun RemoteClient.readFile(remotePath: String): String = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readText() }

fun RemoteClient.writeExecutable(remotePath: String, content: String): Unit =
        writeFile(remotePath, content, GRP_RWX, USR_RWX)

fun RemoteClient.writeFile(remotePath: String, content: String): Unit =
        writeFile(remotePath, content, USR_R, USR_W, GRP_R, GRP_W)

fun RemoteClient.writeFile(remotePath: String, content: String, vararg permissions: FilePermission): Unit = sftp {
    val modes = setOf(CREAT, WRITE, TRUNC)
    val attributes = FileAttributes.Builder()
            .withPermissions(setOf(*permissions))
            .build()
    touchDirectory(remotePath.parent())
    open(remotePath, modes, attributes).use { file -> file.RemoteFileOutputStream().use { stream -> stream.write(content.toByteArray()) } }
}
