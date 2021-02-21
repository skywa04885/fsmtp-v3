package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.net.NIOClientWrapperArgument;

public class StatusCommand implements ImapCommandHandler {


    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {

    }
}
