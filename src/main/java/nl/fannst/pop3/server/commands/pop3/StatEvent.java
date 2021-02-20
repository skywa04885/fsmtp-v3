package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.datatypes.Pair;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandRequirement;
import nl.fannst.pop3.server.commands.PopCommandHandler;

import java.util.ArrayList;
import java.util.UUID;

public class StatEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        ArrayList<Pair<Integer, Integer>> messages = session.getMessages();

        int totalSize = messages.stream().mapToInt(Pair::getSecond).sum();
        new PopReply(PopReply.Indicator.OK, Integer.toString(messages.size()) + ' ' + totalSize).write(client);
    }
}
