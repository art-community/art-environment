/*
 * ART
 *
 * Copyright 2020 ART
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

import constants.LOCALHOST
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.sftp.OpenMode.*
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.time.Duration
import javax.inject.Inject

open class SshConfiguration @Inject constructor() {
    var host: String = LOCALHOST
        private set

    var port: Int? = null
        private set

    var user: String? = null
        private set

    var password: String? = null
        private set

    fun host(host: String) {
        this.host = host
    }

    fun port(port: Int) {
        this.port = port
    }

    fun credentials(user: String, password: String) {
        this.user = user
        this.password = password
    }
}

fun <T> SshConfiguration.ssh(executor: SSHClient.() -> T): T {
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

fun SSHClient.session(executor: Session.() -> Unit) = startSession().use { session ->
    executor(session)
}


fun SSHClient.upload(localPath: Path, remotePath: String) = sftp { put(localPath.toAbsolutePath().toString(), remotePath) }

fun SSHClient.download(remotePath: String, localPath: Path) = sftp { get(remotePath, createDirectories(localPath).toString()) }


fun SSHClient.readFile(remotePath: String): ByteArray = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().readBytes() }

fun SSHClient.readFileText(remotePath: String): String = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readText() }

fun SSHClient.readFileLines(remotePath: String): List<String> = sftp { open(remotePath, setOf(READ)).RemoteFileInputStream().reader().readLines() }


fun SSHClient.writeFile(remotePath: String, content: ByteArray): Unit = sftp { open(remotePath, setOf(CREAT, WRITE, TRUNC)).RemoteFileOutputStream().write(content) }

fun SSHClient.writeFile(remotePath: String, content: String): Unit = sftp { open(remotePath, setOf(CREAT, WRITE, TRUNC)).RemoteFileOutputStream().writer().write(content) }

fun <T> SSHClient.sftp(executor: SFTPClient.() -> T): T = newSFTPClient().use { client ->
    executor(client)
}
