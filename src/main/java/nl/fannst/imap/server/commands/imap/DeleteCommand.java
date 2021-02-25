package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapMailboxArgument;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.models.mail.Message;
import nl.fannst.models.mail.mailbox_v2.Mailbox;
import nl.fannst.models.mail.mailbox_v2.MailboxMeta;
import nl.fannst.models.mail.mailbox_v2.Mailboxes;
import nl.fannst.net.NIOClientWrapperArgument;

public class DeleteCommand implements ImapCommandHandler {
    private static final String MAILBOX_DOESNT_EXISTS_MESSAGE = "delete failure: can't delete mailbox with that name";
    private static final String MAILBOX_CANNOT_BE_DELETED_MESSAGE = "delete failure: mailbox is non-deletable";
    private static final String DELETE_COMPLETED_MESSAGE = "delete completed";

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapMailboxArgument argument = (ImapMailboxArgument) command.getArgument();

        //
        // Gets all the mailboxes.
        //

        Mailboxes mailboxes = Mailboxes.get(session.getAccount().getUUID());
        assert mailboxes != null : "Mailboxes may not be null";

        //
        // Gets and deletes the mailbox.
        //

        // Gets the mailbox by the specified name, if not found send error.
        Mailbox mailbox = mailboxes.getInstanceMailbox(argument.getMailbox());
        if (mailbox == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_DOESNT_EXISTS_MESSAGE).write(client);
            return;
        }

        // Checks if we may delete the mailbox, if not just send error.
        if (mailbox.getMeta().isSystemFlagSet(MailboxMeta.SystemFlags.SOLID)) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_CANNOT_BE_DELETED_MESSAGE).write(client);
            return;
        }

        // Deletes all the messages from the mailbox, and the mailbox itself.
        Message.deleteAllFromMailbox(session.getAccount().getUUID(), mailbox.getID());
        mailboxes.deleteMailbox(argument.getMailbox());
        mailboxes.computeImapFlags();
        mailboxes.update();

        // Sends the OK response to indicate the mailbox is deleted.
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, DELETE_COMPLETED_MESSAGE).write(client);
    }
}
