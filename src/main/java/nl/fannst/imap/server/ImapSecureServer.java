/*(
    Copyright Luke A.C.A. Rieff 2020 - All Rights Reserved
)*/

package nl.fannst.imap.server;

import nl.fannst.Logger;
import nl.fannst.imap.ImapStatusResponse;
import nl.fannst.net.secure.NioSSLClientWrapperArgument;
import nl.fannst.net.secure.NioSSLServer;
import nl.fannst.net.secure.NioSSLServerConfig;

import java.io.IOException;

public class ImapSecureServer extends NioSSLServer {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Logger m_Logger;

    /**
     * Creates new NIOSSLServer
     *
     * @param config   the SSL configuration
     * @param protocol the protocol to use
     * @param hostname the hostname to listen on
     * @param port     the port to listen on
     * @throws Exception possible exception
     */
    public ImapSecureServer(NioSSLServerConfig config, String protocol, String hostname, short port) throws Exception {
        super(config, protocol, hostname, port);

        m_Logger = new Logger("SecureIMAPServer", Logger.Level.TRACE);
    }

    /****************************************************
     * Events
     ****************************************************/

    @Override
    protected void onData(NioSSLClientWrapperArgument client) throws IOException {
        ImapCommon.onData(client);
    }

    @Override
    protected void onConnect(NioSSLClientWrapperArgument client) throws IOException {
        if (Logger.allowTrace()) {
            m_Logger.log("New client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "'");
        }

        new ImapStatusResponse(ImapStatusResponse.Status.OK, "Fannst IMAP Secure ready for requests from "
                + client.getSocketChannel().socket().getInetAddress().getHostAddress()).write(client);
    }

    @Override
    protected void onDisconnect(NioSSLClientWrapperArgument client) {
        if (Logger.allowTrace()) {
            m_Logger.log("Client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' disconnected");
        }
    }
}
