package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandHandler;

import java.net.InetAddress;

public class RsetEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        new PopReply(PopReply.Indicator.OK, "OK flushed " + InetAddress.getLocalHost().getHostName() + '.').write(client);
        session.reset();
    }
}
