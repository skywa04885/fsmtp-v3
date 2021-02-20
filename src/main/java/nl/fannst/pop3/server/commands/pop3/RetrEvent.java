package nl.fannst.pop3.server.commands.pop3;

import nl.fannst.datatypes.Pair;
import nl.fannst.models.mail.Message;
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

public class RetrEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommand.RETR_DELE_Argument argument = PopCommand.RETR_DELE_Argument.parse(command.getArguments());

        ArrayList<Pair<Integer, Integer>> messages = session.getMessages();
        if (argument.getIndex() >= (messages.size() + 1)) {
            new PopReply(PopReply.Indicator.ERR, "Max index: " + (messages.size() + 1)).write(client);
            return;
        }

        // Sets the highest access number
        if (argument.getIndex() > session.getHighestAccessNumber()) {
            session.setHighestAccessNumber(argument.getIndex());
        }

        // Requests the message body and decryption key from MongoDB
        Pair<byte[], byte[]> pair = Message.getMessageBody(session.getAuthenticationUser().getUUID(), messages.get(argument.getIndex() - 1).getFirst());
        assert pair != null;
        assert pair.getFirst() != null;
        assert pair.getSecond() != null;

        // Decrypts the body
        byte[] decrypted = Message.decryptBody(pair.getSecond(), pair.getFirst(), session.getDecryptionKey());

        // Writes the reply with the message content, but first
        //  the OK with the number of octets.
        new PopReply(PopReply.Indicator.OK, messages.get(argument.getIndex() - 1).getSecond() + " octets.").write(client);
        client.write(ByteBuffer.wrap(decrypted));
        client.write(ByteBuffer.wrap("\r\n.\r\n".getBytes()));
    }
}
