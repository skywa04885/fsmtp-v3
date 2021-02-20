package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.net.NIOClientWrapperArgument;

public class LogoutCommand implements ImapCommandHandler {
    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {

    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        new ImapResponse(null, ImapResponse.Type.BYE, "Luke's IMAP logging out").write(client);
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, "We'll miss you!").write(client);

        client.close();
    }
}
