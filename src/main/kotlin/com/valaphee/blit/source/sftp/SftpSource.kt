/*
 * Copyright (c) 2021, Valaphee.
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

package com.valaphee.blit.source.sftp

import com.valaphee.blit.source.NotFoundException
import com.valaphee.blit.source.Source
import io.ktor.utils.io.pool.DefaultPool
import io.ktor.utils.io.pool.useInstance
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.sshd.client.SshClient
import org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.client.SftpClientFactory
import org.apache.sshd.sftp.common.SftpConstants
import org.apache.sshd.sftp.common.SftpException
import java.nio.file.Paths

/**
 * @author Kevin Ludwig
 */
class SftpSource(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val privateKey: String,
    private val connectionPoolSize: Int
) : Source<SftpEntry> {
    internal val semaphore = Semaphore(connectionPoolSize)
    internal val pool = object : DefaultPool<SftpClient>(connectionPoolSize) {
        override fun produceInstance(): SftpClient {
            val sshSession = sshClient.connect(username, host, port).verify().session
            if (password.isNotEmpty()) sshSession.addPasswordIdentity(password)
            if (privateKey.isNotEmpty()) OpenSSHKeyPairResourceParser.INSTANCE.loadKeyPairs(null, Paths.get(privateKey), { _, _, _ -> TODO() }).firstOrNull()?.let { sshSession.addPublicKeyIdentity(it) }
            sshSession.auth().verify()
            return SftpClientFactory.instance().createSftpClient(sshSession)
        }

        override fun clearInstance(instance: SftpClient) = if (instance.isOpen) instance else {
            disposeInstance(instance)
            produceInstance()
        }

        override fun disposeInstance(instance: SftpClient) {
            instance.session.close()
        }
    }

    override val home get() = runBlocking { semaphore.withPermit { pool.useInstance { it.session.executeRemoteCommand("pwd").lines().first() } } }

    override suspend fun get(path: String) = try {
        semaphore.withPermit { pool.useInstance { SftpEntry(this, path, it.stat(path)) } }
    } catch (ex: SftpException) {
        when (ex.status) {
            SftpConstants.SSH_FX_NO_SUCH_FILE -> throw NotFoundException(path)
            else -> throw ex
        }
    }

    override fun close() {
        pool.close()
    }

    companion object {
        internal val sshClient = SshClient.setUpDefaultClient().apply {
            /*NamedFactory.setUpBuiltinFactories(false, BuiltinCompressions.VALUES)*/
            start()
        }
    }
}
