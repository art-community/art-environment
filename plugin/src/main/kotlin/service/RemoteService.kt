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
import logger.error
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.OpenMode.*
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FilePermission
import net.schmizz.sshj.xfer.FilePermission.*
import plugin.plugin
import java.io.InputStream
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

fun RemoteConfiguration.touchDirectory(path: String) = ssh { execute("mkdir -p $path") }

fun RemoteConfiguration.touchFile(path: String) = ssh { execute("touch $path") }

fun RemoteConfiguration.runtimeDirectory() = touchDirectory("${directory()}/$RUNTIME")

fun RemoteConfiguration.projectsDirectory() = touchDirectory("${directory()}/$PROJECTS")

fun RemoteConfiguration.projectDirectory(name: String) = touchDirectory("${directory()}/$PROJECTS/$name")

fun RemoteClient.execute(command: String) = execute(configuration.runtimeDirectory(), )

fun RemoteClient.execute(remotePath: String, script: () -> String) = session {
    writeFile(remotePath, script())
    execute("""
        bash -c "cd /home/anton/art && nohup bash $remotePath 1>log.stdout 2>log.stderr &"
    """.trimIndent())
}


fun RemoteRemoteClient.upload(localPath: Path, remotePath: String) = sftp { put(localPath.toAbsolutePath().toString(), remotePath) }
fun RemoteRemoteClient.download(remotePath: String, localPath: Path) = sftp { get(remotePath, createDirectories(localPath).toString()) }

fun RemoteRemoteClient.readFile(remotePath: String): String = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readText() }

fun RemoteRemoteClient.writeExecutable(remotePath: String, content: String): Unit =
        writeFile(remotePath, content, GRP_RWX, USR_RWX)

fun RemoteRemoteClient.writeFile(remotePath: String, content: String): Unit =
        writeFile(remotePath, content, USR_R, USR_W, GRP_R, GRP_W)

fun RemoteRemoteClient.writeFile(remotePath: String, content: String, vararg permissions: FilePermission): Unit = sftp {
    val modes = setOf(CREAT, WRITE, TRUNC)
    val attributes = FileAttributes.Builder()
            .withPermissions(setOf(*permissions))
            .build()
    open(remotePath, modes, attributes).use { file -> file.RemoteFileOutputStream().use { stream -> stream.writer().write(content) } }
}

private fun consoleLog(output: InputStream) {
    output.reader()
            .readLines()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> plugin.project.error(line) }
}
