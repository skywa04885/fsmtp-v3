package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandRequirement;
import nl.fannst.pop3.server.commands.PopCommandHandler;

public class LastEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        new PopReply(PopReply.Indicator.OK, Integer.toString(session.getHighestAccessNumber())).write(client);
    }
}
