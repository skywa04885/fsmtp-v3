package nl.fannst.net.secure;

import nl.fannst.Logger;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SSLNioPeer {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected ByteBuffer m_AppReadableData;
    protected ByteBuffer m_AppEncryptedData;

    protected ByteBuffer m_NetReadableData;
    protected ByteBuffer m_NetEncryptedData;

    protected ExecutorService m_Executor = Executors.newSingleThreadExecutor();

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Performs an SSL handshake
     *
     * @param socketChannel the channel
     * @param sslEngine the engine
     * @return success or fail
     */
    protected boolean performHandshake(SocketChannel socketChannel, SSLEngine sslEngine) {
        m_AppEncryptedData.clear();
        m_AppEncryptedData.clear();
        m_NetEncryptedData.clear();
        m_NetReadableData.clear();

        try {
            for (;;) {
                SSLEngineResult.HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
                switch (handshakeStatus) {
                    /* Simple stuff */

                    case NOT_HANDSHAKING, FINISHED -> {
                        return true;
                    }

                    case NEED_TASK -> {
                        Runnable task;
                        while ((task = sslEngine.getDelegatedTask()) != null) {
                            m_Executor.execute(task);
                        }
                    }

                    /* Fucked up stuff */

                    case NEED_WRAP -> {
                        if (!onNeedWrap(socketChannel, sslEngine)) {
                            return false;
                        }
                    }

                    case NEED_UNWRAP -> {
                        if (!onNeedUnwrap(socketChannel, sslEngine)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean onNeedWrap(SocketChannel socketChannel, SSLEngine sslEngine) throws Exception {
        m_AppEncryptedData.clear();

        SSLEngineResult result;
        try {
            result = sslEngine.wrap(m_AppReadableData, m_AppEncryptedData);
        } catch (SSLException e) {
            if (Logger.allowTrace()) e.printStackTrace();
            sslEngine.closeOutbound();
            return false;
        }

        switch (result.getStatus()) {
            case OK -> {
                m_AppEncryptedData.flip();
                while (m_AppEncryptedData.hasRemaining()) {
                    socketChannel.write(m_AppEncryptedData);
                }
            }

            case CLOSED -> {
                try {
                    m_AppEncryptedData.flip();
                    while (m_AppEncryptedData.hasRemaining()) {
                        socketChannel.write(m_AppEncryptedData);
                    }
                } catch (Exception e) {
                    if (Logger.allowTrace()) e.printStackTrace();
                }
            }

            case BUFFER_OVERFLOW -> m_AppEncryptedData = enlargePacketBuffer(sslEngine, m_AppEncryptedData);
            case BUFFER_UNDERFLOW -> throw new Exception("Underflow occured.");
        }

        return true;
    }

    protected boolean onNeedUnwrap(SocketChannel socketChannel, SSLEngine sslEngine) throws IOException {
        m_NetReadableData.clear();

        int len;
        if ((len = socketChannel.read(m_NetEncryptedData)) == -1) {
            if (sslEngine.isInboundDone() && sslEngine.isOutboundDone()) {
                return false;
            }

            try {
                sslEngine.closeOutbound();
                sslEngine.closeInbound();
            } catch (Exception e) {
                if (Logger.allowTrace()) e.printStackTrace();
            }

            return false;
        }

        SSLEngineResult result;
        try {
            m_NetEncryptedData.flip();
            result = sslEngine.unwrap(m_NetEncryptedData, m_NetReadableData);
            m_NetEncryptedData.compact();
        } catch (SSLException e) {
            if (Logger.allowTrace()) e.printStackTrace();
            sslEngine.closeOutbound();
            return false;
        }

        switch (result.getStatus()) {
            case OK -> {}
            case BUFFER_OVERFLOW -> m_NetReadableData = enlargeApplicationBuffer(sslEngine, m_NetReadableData);
            case BUFFER_UNDERFLOW -> m_NetEncryptedData = handleBufferUnderflow(sslEngine, m_NetEncryptedData);
            case CLOSED -> {
                if (sslEngine.isOutboundDone()) return false;

                sslEngine.closeOutbound();
                return false;
            }
        }

        return true;
    }

    protected ByteBuffer handleBufferUnderflow(SSLEngine sslEngine, ByteBuffer buffer) {
        if (sslEngine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer newBuffer = enlargePacketBuffer(sslEngine, buffer);
            buffer.flip();
            newBuffer.put(buffer);
            return newBuffer;
        }
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine sslEngine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, sslEngine.getSession().getApplicationBufferSize());
    }

    protected ByteBuffer enlargePacketBuffer(SSLEngine sslEngine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, sslEngine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    protected void closeSocketChannel(SocketChannel socketChannel, SSLEngine sslEngine) throws Exception {
        sslEngine.closeOutbound();
        performHandshake(socketChannel, sslEngine);
        socketChannel.close();
    }

    protected void handleEOF(SocketChannel socketChannel, SSLEngine sslEngine) throws Exception {
        try {
            sslEngine.closeInbound();
        } catch (SSLException ignore) {}

        closeSocketChannel(socketChannel, sslEngine);
    }

    protected KeyManager[] createKeyManagers(String file, String keystorePassphrase, String keyPassphrase) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");

        try (FileInputStream keystoreStream = new FileInputStream(file)) {
            keyStore.load(keystoreStream, keystorePassphrase.toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyPassphrase.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }
}
