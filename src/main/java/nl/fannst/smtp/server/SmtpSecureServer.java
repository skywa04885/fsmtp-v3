package nl.fannst.smtp.server;

import nl.fannst.Logger;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.secure.NioSSLClientWrapperArgument;
import nl.fannst.net.secure.NioSSLServer;
import nl.fannst.net.secure.NioSSLServerConfig;
import nl.fannst.smtp.SmtpReply;
import nl.fannst.smtp.server.session.SmtpServerSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SmtpSecureServer extends NioSSLServer {
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
    public SmtpSecureServer(NioSSLServerConfig config, String protocol, String hostname, short port) throws Exception {
        super(config, protocol, hostname, port);

        m_Logger = new Logger("SecureSMTPServer", Logger.Level.TRACE);
    }

    /****************************************************
     * Events
     ****************************************************/

    @Override
    protected void onData(NioSSLClientWrapperArgument client) throws IOException {
        SmtpCommon.handleData((NIOClientWrapperArgument) client, (SmtpServerSession) client.getClientWrapper().attachment());
    }

    @Override
    protected void onConnect(NioSSLClientWrapperArgument client) throws IOException {
        m_Logger.logTrace(() -> "New client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' ;)");

        client.getClientWrapper().attach(new SmtpServerSession());
        new SmtpReply(220, "Fannst ESMTP Secure Ready at: "
                + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())).write(client);
    }

    @Override
    protected void onDisconnect(NioSSLClientWrapperArgument client) {
        m_Logger.logTrace(() -> "Client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' disconnected ;(");
    }
}
