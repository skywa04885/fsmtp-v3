package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapMailboxArgument;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.models.mail.mailbox_v2.Mailboxes;
import nl.fannst.net.NIOClientWrapperArgument;

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
        // Gets the users mailboxes
        //

        Mailboxes mailboxes = Mailboxes.get(session.getAccount().getUUID());
        assert mailboxes != null : "Mailboxes may not be null!";

        //
        // Checks if not exists.
        //

        // Checks if the mailbox does not exist yet
        if (mailboxes.getInstanceMailbox(argument.getMailbox()) != null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_EXISTS_MESSAGE).write(client);
            return;
        }

        //
        // Creates the new mailbox.
        //

        // Inserts the new mailbox in the tree, and performs the flag creation
        //  such as HAS_CHILDREN and HAS_NO_CHILDREN.
        mailboxes.insertMailbox(argument.getMailbox());
        mailboxes.computeImapFlags();

        // Performs the update operation

        //
        // Finishes
        //

        // Sends the success reply to the client.
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, CREATE_COMPLETED_MESSAGE).write(client);
    }
}
