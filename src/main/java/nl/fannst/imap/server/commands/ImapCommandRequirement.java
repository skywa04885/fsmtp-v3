package nl.fannst.imap.server.commands;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.imap.server.session.ImapSessionState;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class ImapCommandRequirement {
    public static void requireAuthenticated(NIOClientWrapperArgument client, ImapCommand command) throws IOException {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();

        if (session.getState() == ImapSessionState.NOT_AUTHENTICATED) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "authentication state required").write(client);
        }
    }
}