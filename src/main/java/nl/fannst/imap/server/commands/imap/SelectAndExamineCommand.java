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
import nl.fannst.models.mail.Message;
import nl.fannst.models.mail.mailbox_v2.Mailbox;
import nl.fannst.models.mail.mailbox_v2.MailboxMeta;
import nl.fannst.models.mail.mailbox_v2.Mailboxes;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class SelectAndExamineCommand {
    private static final String EXAMINE_COMPLETED_MESSAGE = "examine completed, now in selected state";
    private static final String EXAMINE_NO_SUCH_MAILBOX_MESSAGE = "examine failure, no such mailbox";

    private static final String SELECT_COMPLETED_MESSAGE = "select completed, now in selected state";
    private static final String SELECT_NO_SUCH_MAILBOX_MESSAGE = "select failure, no such mailbox";

    private static void sendMailboxStatus(NIOClientWrapperArgument client, ImapCommand command, Mailbox mailbox) throws IOException {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();

        ArrayList<Integer> recent = Message.getRecent(session.getAccount().getUUID(), mailbox.getID());
        ArrayList<Integer> unseen = Message.getUIDsWhereFlagClear(session.getAccount().getUUID(), mailbox.getID(),
                Message.Flag.SEEN.getMask());

        int nextUID = Objects.requireNonNull(BasicAccount.getNextUid(session.getAccount().getUUID()));
        new ImapMailboxInfoResponse(mailbox.getMeta().getMessageCount(), recent.size(), unseen.size(), Message.Flag.values(), nextUID,
                Integer.MAX_VALUE).write(client);
    }

    /****************************************************
     * Status
     ****************************************************/

    /****************************************************
     * Examine
     ****************************************************/

    public static class ExamineCommand implements ImapCommandHandler {
        @Override
        public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
            ImapCommandRequirement.requireAuthenticated(client, command);
        }

        @Override
        public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
            ImapSession session = (ImapSession) client.getClientWrapper().attachment();
            ImapMailboxArgument argument = (ImapMailboxArgument) command.getArgument();

            //
            // Gets the mailboxes
            //

            Mailboxes mailboxes = Mailboxes.get(session.getAccount().getUUID());
            assert mailboxes != null : "Mailboxes may not be null!";

            //
            // Get and selects the mailbox
            //

            Mailbox mailbox;
            if ((mailbox = mailboxes.getInstanceMailbox(argument.getMailbox())) == null) {
                new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, EXAMINE_NO_SUCH_MAILBOX_MESSAGE).write(client);
                return;
            }

            sendMailboxStatus(client, command, mailbox);
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, SELECT_COMPLETED_MESSAGE, false,
                    new ImapResponse.StatusCode(ImapResponse.StatusCode.Type.READ_ONLY, null), null).write(client);

            session.setState(ImapSessionState.SELECTED_NO_CHANGES);
            session.setMailboxes(mailbox);
        }
    }

    /****************************************************
     * Select
     ****************************************************/

    public static class SelectCommand implements ImapCommandHandler {
        @Override
        public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
            ImapCommandRequirement.requireAuthenticated(client, command);
        }

        @Override
        public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
            ImapSession session = (ImapSession) client.getClientWrapper().attachment();
            ImapMailboxArgument argument = (ImapMailboxArgument) command.getArgument();


            //
            // Gets the mailboxes
            //

            Mailboxes mailboxes = Mailboxes.get(session.getAccount().getUUID());
            assert mailboxes != null : "Mailboxes may not be null!";

            //
            // Get and selects the mailbox
            //

            Mailbox mailbox;
            if ((mailbox = mailboxes.getInstanceMailbox(argument.getMailbox())) == null) {
                new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, EXAMINE_NO_SUCH_MAILBOX_MESSAGE).write(client);
                return;
            }

            sendMailboxStatus(client, command, mailbox);
            ImapResponse.StatusCode statusCode = new ImapResponse.StatusCode(mailbox.getMeta()
                    .isSystemFlagSet(MailboxMeta.SystemFlags.READ_ONLY) ? ImapResponse.StatusCode.Type.READ_ONLY :
                    ImapResponse.StatusCode.Type.READ_WRITE, null);

            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, SELECT_COMPLETED_MESSAGE, false, statusCode,
                    null).write(client);

            session.setState(ImapSessionState.SELECTED);
            session.setMailboxes(mailbox);
        }
    }

}
