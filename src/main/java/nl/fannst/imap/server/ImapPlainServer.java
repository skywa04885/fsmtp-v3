package nl.fannst.imap.server;

import nl.fannst.Logger;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.net.plain.PlainNIOServer;
import nl.fannst.net.secure.NioSSLClientWrapperArgument;

import java.io.IOException;

public class ImapPlainServer extends PlainNIOServer {
    private final Logger m_Logger;

    /**
     * Creates a new IMAP Server instance
     *
     * @param hostname the hostname to listen on
     * @param port the port to listen on
     */
    public ImapPlainServer(String hostname, short port) throws IOException {
        super(hostname, port);

        m_Logger = new Logger("IMAPSMTPServer", Logger.Level.TRACE);
    }

    /****************************************************
     * Events
     ****************************************************/

    @Override
    protected void onData(PlainNIOClientArgument client) throws IOException {
        ImapCommon.onData(client);
    }

    @Override
    protected void onConnect(PlainNIOClientArgument client) throws IOException {
        if (Logger.allowTrace()) {
            m_Logger.log("New client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "'");
        }

        client.getClientWrapper().attach(new ImapSession());

        new ImapResponse(null, ImapResponse.Type.OK, "Luke's IMAP ready for requests from "
                + client.getSocketChannel().socket().getInetAddress().getHostAddress()).write(client);
    }

    @Override
    protected void onDisconnect(PlainNIOClientArgument client) {
        if (Logger.allowTrace()) {
            m_Logger.log("Client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' disconnected");
        }
    }
}
