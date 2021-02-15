package nl.fannst.net.secure;

import nl.fannst.net.NIOClientWrapper;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public class NioSSLClientWrapper extends NIOClientWrapper {
    private final SSLEngine m_SSLEngine;

    public NioSSLClientWrapper(SocketChannel socketChannel, SSLEngine sslEngine) {
        super(socketChannel);
        m_SSLEngine = sslEngine;
    }

    public SSLEngine getSSLEngine() {
        return m_SSLEngine;
    }
}
