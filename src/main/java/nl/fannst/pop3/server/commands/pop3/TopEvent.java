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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class TopEvent implements PopCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommandRequirement.mustBeAuthenticatedState(session);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, PopServerSession session, PopCommand command) throws Exception {
        PopCommand.TOP_Argument argument = PopCommand.TOP_Argument.parse(command.getArguments());

        ArrayList<Pair<Integer, Integer>> messages = session.getMessages();
        if (argument.getIndex() > (messages.size() + 1)) {
            new PopReply(PopReply.Indicator.ERR, "Max index: " + (messages.size() + 1)).write(client);
            return;
        }

        // Requests the message body and decryption key from MongoDB
        Pair<byte[], byte[]> pair = Message.getMessageBody(session.getAuthenticationUser().getUUID(), messages.get(argument.getIndex() - 1).getFirst());
        assert pair != null;

        // Decrypts the body
        byte[] decrypted = Message.decryptBody(pair.getSecond(), pair.getFirst(), session.getDecryptionKey());
        new PopReply(PopReply.Indicator.OK, "Message top follows.").write(client);

        // Sends the headers, and the specified number of lines from the body
        Scanner scanner = new Scanner(new String(decrypted, StandardCharsets.UTF_8));
        boolean headersEnded = false;
        int n = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.trim().isEmpty() && !headersEnded) {
                headersEnded = true;
                if (argument.getBodyLine() <= 0) {
                    break;
                }
            } else if (headersEnded) {
                if (n++ >= argument.getBodyLine()) {
                    break;
                }
            }

            client.write(ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8)));
            client.write(ByteBuffer.wrap("\r\n".getBytes(StandardCharsets.UTF_8)));
        }

        // Writes the reply with the message content, but first
        //  the OK with the number of octets.
        client.write(ByteBuffer.wrap(".\r\n".getBytes()));
    }
}
