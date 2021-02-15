package nl.fannst.pop3.server;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;
import nl.fannst.pop3.PopCommand;
import nl.fannst.pop3.PopReply;
import nl.fannst.pop3.server.commands.PopCommandEvent;

import java.io.IOException;

public class POP3Server {
    public static void handleCommand(PopServerSession session, NIOClientWrapperArgument client) throws IOException {
        try {
            String raw = client.getClientWrapper().getSegmentedBuffer().read();
            PopCommand command = PopCommand.parse(raw.substring(0, raw.length() - 1));

            PopCommandEvent event = PopCommandEvent.get(command.getType());
            if (event == null) {
                new PopReply(PopReply.Indicator.ERR, "No event registered for command: '" + command.getType().toString() + "'.").write(client);
                return;
            }

            event.getHandler().allowed(client, session, command);
            event.getHandler().handle(client, session, command);
        } catch (PopCommand.UnrecognizedException | PopCommand.InvalidArgumentException | PopCommand.SequenceException e) {
            new PopReply(PopReply.Indicator.ERR, e.getMessage()).write(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleData(PopServerSession session, NIOClientWrapperArgument client) throws IOException {
        SegmentedBuffer segmentedBuffer = client.getClientWrapper().getSegmentedBuffer();

        boolean more = false;
        do {
            if (segmentedBuffer.lineAvailable()) {
                handleCommand(session, client);
                more = client.getClientWrapper().getSegmentedBuffer().lineAvailable();
            }
        } while (more);

        segmentedBuffer.shift();
    }
}
