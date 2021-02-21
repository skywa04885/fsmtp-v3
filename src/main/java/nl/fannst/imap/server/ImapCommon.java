/*(
    Copyright Luke A.C.A. Rieff 2020 - All Rights Reserved
)*/

package nl.fannst.imap.server;

import nl.fannst.datatypes.SegmentedBuffer;
import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.server.commands.ImapCommandEvent;
import nl.fannst.net.NIOClientWrapper;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class ImapCommon {
    private static void onCommand(NIOClientWrapperArgument client) throws IOException {
        NIOClientWrapper clientWrapper = client.getClientWrapper();
        SegmentedBuffer buffer = clientWrapper.getSegmentedBuffer();

        String line = buffer.read();

        try {
            ImapCommand command = ImapCommand.parse(line);

            ImapCommandEvent event;
            if ((event = ImapCommandEvent.get(command.getType())) == null)
                throw new Exception("event not registered");

            event.getHandler().allowed(client, command);
            event.getHandler().handle(client, command);
        } catch (Exception e) {
            e.printStackTrace();
            new ImapResponse(null, ImapResponse.Type.BAD, e.getMessage()).write(client);
        }
    }

    public static void onData(NIOClientWrapperArgument client) throws IOException {
        NIOClientWrapper clientWrapper = client.getClientWrapper();
        SegmentedBuffer buffer = clientWrapper.getSegmentedBuffer();

        boolean more = false;
        do {
            if (buffer.lineAvailable()) {
                onCommand(client);

                more = buffer.lineAvailable();
            }
        } while (more);

        buffer.shift();
    }
}
