package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapMailboxArgument;
import nl.fannst.imap.fancy_responses.ImapMailboxInfoResponse;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.imap.server.session.ImapSessionState;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.mail.Mailbox;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;

import java.util.ArrayList;
import java.util.Objects;

public class CreateCommand implements ImapCommandHandler {
    private static final String MAILBOX_EXISTS_MESSAGE = "create failure: can't create mailbox with that name";
    private static final String CREATE_COMPLETED_MESSAGE = "create completed";

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapMailboxArgument argument = (ImapMailboxArgument) command.getArgument();

        //
        // Checks if not exists.
        //

        // Checks if the mailbox does not exist yet
        if (Mailbox.getByName(session.getAccount().getUUID(), argument.getMailbox()) != null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_EXISTS_MESSAGE).write(client);
            return;
        }

        //
        // Creates the new mailbox.
        //

        // Gets the largest ID of the mailboxes, increment it and use it as ID.
        Integer largestMailboxID;
        if ((largestMailboxID = Mailbox.getLargestID(session.getAccount().getUUID())) == null)
            largestMailboxID = 0;

        // Creates the new mailbox, and saves it to MongoDB.
        Mailbox mailbox = new Mailbox(session.getAccount().getUUID(), largestMailboxID + 1, argument.getMailbox(),
                0, 0);

        mailbox.save();

        //
        // Finishes
        //

        // Sends the success reply to the client.
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, CREATE_COMPLETED_MESSAGE).write(client);
    }
}
