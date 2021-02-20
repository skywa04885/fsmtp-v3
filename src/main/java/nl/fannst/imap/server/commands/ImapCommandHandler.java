package nl.fannst.imap.server.commands;

import nl.fannst.imap.ImapCommand;
import nl.fannst.net.NIOClientWrapperArgument;

public interface ImapCommandHandler {
    void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception;
    void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception;
}
