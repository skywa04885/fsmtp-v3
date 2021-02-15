package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.datatypes.Pair;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.PopServerSession;
import nl.fannst.pop3.server.commands.PopCommandRequirement;
import nl.fannst.pop3.server.commands.PopCommandHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class UidlEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        ArrayList<Pair<UUID, Integer>> messages = session.getMessages();

        new PopReply(PopReply.Indicator.OK, "Mailbox listing follows.").write(client);

        int i = 0;
        for (Pair<UUID, Integer> message : messages) {
            String line = Integer.toString(++i) + ' ' + message.getFirst().toString() + "\r\n";
            client.write(ByteBuffer.wrap(line.getBytes()));
        }

        client.write(ByteBuffer.wrap(".\r\n".getBytes()));
    }
}
