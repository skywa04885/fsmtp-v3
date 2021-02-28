package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapRenameArgument;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.models.mail.mailbox_v2.Mailbox;
import nl.fannst.models.mail.mailbox_v2.MailboxMeta;
import nl.fannst.models.mail.mailbox_v2.Mailboxes;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class RenameCommand implements ImapCommandHandler {
    private static final String MAILBOX_DOESNT_EXISTS_MESSAGE = "rename failure: can't rename mailbox with that name, can't rename to mailbox with that name";
    private static final String CANNOT_RENAME_MESSAGE = "rename failure: mailbox is non-rename-able";
    private static final String RENAME_COMPLETED_MESSAGE = "rename completed";

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    private boolean renameMailbox(NIOClientWrapperArgument client, ImapCommand command) throws IOException {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapRenameArgument argument = (ImapRenameArgument) command.getArgument();

        // Gets all the mailboxes for the user, if this returns null we will throw
        //  an assertion error, since it should not occur.
        Mailboxes mailboxes = Mailboxes.get(session.getAccount().getUUID());
        assert mailboxes != null : "There cannot be an account without mailboxes.";

        // Gets the mailbox by the specified name, if it does not exist trow an does
        //  not exist error to the client.
        Mailbox mailbox = mailboxes.getInstanceMailbox(argument.getOriginal());
        if (mailbox == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_DOESNT_EXISTS_MESSAGE)
                    .write(client);
            return false;
        }

        // Checks if we may delete the mailbox, if not just send error.
        if (mailbox.getMeta().isSystemFlagSet(MailboxMeta.SystemFlags.SYSTEM)) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, CANNOT_RENAME_MESSAGE).write(client);
            return false;
        }

        // Sets the new name for the mailbox.
        mailbox.setName(argument.getNew());

        // Since one of the sub-elements changed, we will set the heads
        //  changed bit, and perform an tree-wise update operation.
        mailboxes.setHeadsChangeBit();

        return true;
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        // Performs the rename operation.
        if (!renameMailbox(client, command))
            return;

        // Sends the OK response to indicate the mailbox is deleted.
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, RENAME_COMPLETED_MESSAGE).write(client);
    }
}
