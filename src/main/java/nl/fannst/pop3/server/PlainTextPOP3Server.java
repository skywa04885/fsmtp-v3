package nl.fannst.pop3.server;

import nl.fannst.Logger;
import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.net.plain.PlainNIOServer;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.commands.PopCommandEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlainTextPOP3Server extends PlainNIOServer {
    public PlainTextPOP3Server(String hostname, short port) throws IOException {
        super(hostname, port);
    }


    @Override
    protected void onData(PlainNIOClientArgument client) throws IOException {
        PopServerSession session = (PopServerSession) client.getClientWrapper().attachment();
        POP3Server.handleData(session, client);
    }

    @Override
    protected void onConnect(PlainNIOClientArgument client) throws IOException {
        client.getClientWrapper().attach(new PopServerSession());
        new PopReply(PopReply.Indicator.OK, "Fannst POP3 server ready at "
                + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())).write(client);
    }

    @Override
    protected void onDisconnect(PlainNIOClientArgument client) {

    }

}
