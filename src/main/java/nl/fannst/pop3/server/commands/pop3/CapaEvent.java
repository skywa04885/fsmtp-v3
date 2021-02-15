package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandHandler;

import java.util.ArrayList;

public class CapaEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        new PopReply(PopReply.Indicator.OK, "Capabilities follow.").write(client);

        ArrayList<String> lines = new ArrayList<String>();

        lines.add("EXPIRE NEVER");
        lines.add("LOGIN-DELAY 0");
        lines.add("TOP");
        lines.add("UIDL");
        lines.add("USER");
        lines.add("RESP-CODES");
        lines.add("AUTH-RESP-CODE");
        lines.add("PIPELINING");
        lines.add("IMPLEMENTATION Fannst POP3 by Luke A.C.A. Rieff");

        PopReply.writeMultiline(lines, client);
    }
}
