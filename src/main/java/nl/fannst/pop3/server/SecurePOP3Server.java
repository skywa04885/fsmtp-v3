package nl.fannst.pop3.server;

import nl.fannst.net.NIOClientWrapper;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.secure.NioSSLClientWrapperArgument;
import nl.fannst.net.secure.NioSSLServer;
import nl.fannst.net.secure.NioSSLServerConfig;
import nl.fannst.pop3.PopReply;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecurePOP3Server extends NioSSLServer {
    /**
     * Creates new NIOSSLServer
     *
     * @param config   the SSL configuration
     * @param protocol the protocol to use
     * @param hostname the hostname to listen on
     * @param port     the port to listen on
     * @throws Exception possible exception
     */
    public SecurePOP3Server(NioSSLServerConfig config, String protocol, String hostname, short port) throws Exception {
        super(config, protocol, hostname, port);
    }

    @Override
    protected void onData(NioSSLClientWrapperArgument client) throws IOException {
        PopServerSession session = (PopServerSession) client.getClientWrapper().attachment();
        POP3Server.handleData(session, (NIOClientWrapperArgument) client);
    }

    @Override
    protected void onConnect(NioSSLClientWrapperArgument client) throws IOException {
        client.getClientWrapper().attach(new PopServerSession());
        new PopReply(PopReply.Indicator.OK, "Fannst POP3 Secure server ready at "
                + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())).write(client);
    }

    @Override
    protected void onDisconnect(NioSSLClientWrapperArgument client) {

    }
}
