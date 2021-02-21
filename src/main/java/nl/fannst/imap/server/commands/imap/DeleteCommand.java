package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapMailboxArgument;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.models.mail.Mailbox;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class DeleteCommand implements ImapCommandHandler {
    private static final String MAILBOX_DOESNT_EXISTS_MESSAGE = "delete failure: can't delete mailbox with that name";
    private static final String MAILBOX_CANNOT_BE_DELETED_MESSAGE = "delete failure: mailbox is non-deletable";
    private static final String DELETE_COMPLETED_MESSAGE = "delete completed";

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    /**
     * Gets the mailbox specified by the client, we send an error if the mailbox was not found
     *  or was declared as solid ( non-deletable ).
     *
     * @param client the client.
     * @param command the command
     * @return the mailbox.
     * @throws IOException possible exception,
     */
    private Mailbox getMailbox(NIOClientWrapperArgument client, ImapCommand command) throws IOException {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapMailboxArgument argument = (ImapMailboxArgument) command.getArgument();

        // Gets the mailbox by the specified name, if not found send error.
        Mailbox mailbox = Mailbox.getByName(session.getAccount().getUUID(), argument.getMailbox());
        if (mailbox == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_DOESNT_EXISTS_MESSAGE).write(client);
            return null;
        }

        // Checks if we may delete the mailbox, if not just send error.
        if (mailbox.isFlagSet(Mailbox.Flag.Solid)) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_CANNOT_BE_DELETED_MESSAGE).write(client);
            return null;
        }

        return mailbox;
    }


    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();

        // Gets the mailbox, if it returns null either it is not allowed, or it was
        //  not found, so return.
        Mailbox mailbox = getMailbox(client, command);
        if (mailbox == null) return;

        // Deletes all the messages from the mailbox, and the mailbox itself.
        Message.deleteAllFromMailbox(session.getAccount().getUUID(), mailbox.getMailboxID());
        Mailbox.delete(session.getAccount().getUUID(), mailbox.getMailboxID());

        // Sends the OK response to indicate the mailbox is deleted.
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, DELETE_COMPLETED_MESSAGE).write(client);
    }
}
