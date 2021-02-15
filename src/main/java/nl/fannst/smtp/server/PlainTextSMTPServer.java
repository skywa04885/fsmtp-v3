package nl.fannst.smtp.server;

import nl.fannst.Logger;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.net.plain.PlainNIOServer;
import nl.fannst.smtp.SmtpReply;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlainTextSMTPServer extends PlainNIOServer {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Logger m_Logger;

    /**
     * Creates a new SMTP Server instance
     *
     * @param hostname the hostname to listen on
     * @param port the port to listen on
     */
    public PlainTextSMTPServer(String hostname, short port) throws IOException {
        super(hostname, port);

        m_Logger = new Logger("PlainSMTPServer", Logger.Level.TRACE);
    }

    /****************************************************
     * Override Methods
     ****************************************************/

    @Override
    protected void onData(PlainNIOClientArgument client) throws IOException {
        SmtpServerSession session = (SmtpServerSession) client.getClientWrapper().attachment();
        SMTPServer.handleData(client, session);
    }

    @Override
    protected void onConnect(PlainNIOClientArgument client) throws IOException {
        if (Logger.allowTrace())
            m_Logger.log("New client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' ;)");

        client.getClientWrapper().attach(new SmtpServerSession());
        new SmtpReply(220, "Fannst ESMTP Ready at: "
                + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())).write(client);
    }

    @Override
    protected void onDisconnect(PlainNIOClientArgument client) {
        if (Logger.allowTrace())
            m_Logger.log("Client '" + client.getClientWrapper().getSocketChannel().socket().getRemoteSocketAddress() + "' disconnected ;(");
    }
}
