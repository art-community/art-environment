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

import configuration.RemoteExecutionConfiguration
import logger.error
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.OpenMode.*
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import net.schmizz.sshj.xfer.FilePermission.GRP_RWX
import net.schmizz.sshj.xfer.FilePermission.USR_RWX
import plugin.plugin
import java.io.InputStream
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.time.Duration


fun <T> RemoteExecutionConfiguration.ssh(executor: SSHClient.() -> T): T {
    SSHClient().use { client ->
        with(client) {
            addHostKeyVerifier(PromiscuousVerifier())
            addAlgorithmsVerifier { true }
            port?.let { sshPort -> connect(host, sshPort) } ?: connect(host)
            authPassword(user, password)
            timeout = Duration.ofSeconds(30).toMillis().toInt()
            return executor(client)
        }
    }
}

fun <T> SSHClient.sftp(executor: SFTPClient.() -> T): T = newSFTPClient().use { client ->
    executor(client)
}


fun SSHClient.session(executor: Session.() -> Unit) = startSession().use { session ->
    executor(session)
}

fun SSHClient.execute(command: String) = session {
    val exec = exec(command)
}

fun SSHClient.execute(remotePath: String, script: () -> String) {
    writeFile(remotePath, script())
    execute("""
        bash -c "cd /home/anton/art && nohup bash $remotePath 1>log.stdout 2>log.stderr &"
    """.trimIndent())
}


fun SSHClient.upload(localPath: Path, remotePath: String) = sftp { put(localPath.toAbsolutePath().toString(), remotePath) }

fun SSHClient.download(remotePath: String, localPath: Path) = sftp { get(remotePath, createDirectories(localPath).toString()) }


fun SSHClient.readFileBytes(remotePath: String): ByteArray = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().readBytes() }

fun SSHClient.readFileText(remotePath: String): String = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readText() }


fun SSHClient.writeFile(remotePath: String, content: ByteArray): Unit = sftp {
    open(remotePath, setOf(CREAT, WRITE, TRUNC), FileAttributes.Builder().withPermissions(setOf(USR_RWX, GRP_RWX)).build()).use { file ->
        file.RemoteFileOutputStream().use { stream ->
            stream.write(content)
        }
    }
}

fun SSHClient.writeFile(remotePath: String, content: String): Unit = writeFile(remotePath, content.toByteArray())

private fun logExecution(output: InputStream) {
    output.reader()
            .readLines()
            .filter { line -> line.isNotBlank() }
            .forEach { line -> plugin.project.error(line) }
}
