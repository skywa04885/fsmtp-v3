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

public class DeleEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommand.RETR_DELE_Argument argument = PopCommand.RETR_DELE_Argument.parse(command.getArguments());

        ArrayList<Pair<Integer, Integer>> messages = session.getMessages();
        if (argument.getIndex() > (messages.size() + 1)) {
            new PopReply(PopReply.Indicator.ERR, "Max index: " + (messages.size() + 1)).write(client);
            return;
        }

        if (argument.getIndex() > session.getHighestAccessNumber()) {
            session.setHighestAccessNumber(argument.getIndex());
        }

        session.addDeleteMarkedMessage(messages.get(argument.getIndex() - 1).getFirst());
        new PopReply(PopReply.Indicator.OK, "Message " + argument.getIndex() + " marked for delete.").write(client);
    }
}
