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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Select_Examine_Command {
    private static void sendMailboxStatus(NIOClientWrapperArgument client, ImapCommand command, Mailbox mailbox) throws IOException {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();

        ArrayList<Integer> recent = Message.getRecent(session.getAccount().getUUID(), mailbox.getMailboxID());
        ArrayList<Integer> unseen = Message.getUIDsWhereFlagClear(session.getAccount().getUUID(), mailbox.getMailboxID(),
                Message.Flag.SEEN.getMask());

        int nextUID = Objects.requireNonNull(BasicAccount.getNextUid(session.getAccount().getUUID()));
        new ImapMailboxInfoResponse(mailbox.getMessageCount(), recent.size(), unseen.size(), Message.Flag.values(), nextUID,
                Integer.MAX_VALUE - (nextUID - 1)).write(client);

        ImapResponse.StatusCode statusCode = new ImapResponse.StatusCode(mailbox.isFlagSet(Mailbox.Flag.ReadOnly)
                ? ImapResponse.StatusCode.Type.READ_ONLY : ImapResponse.StatusCode.Type.READ_WRITE, null);
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, "EXAMINE completed", false, statusCode,
                null).write(client);
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

            Mailbox mailbox = Mailbox.getByName(session.getAccount().getUUID(), argument.getMailbox());
            if (mailbox == null) {
                new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "no such mailbox").write(client);
                return;
            }

            sendMailboxStatus(client, command, mailbox);
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

            // Gets the mailbox.
            Mailbox mailbox = Mailbox.getByName(session.getAccount().getUUID(), argument.getMailbox());
            if (mailbox == null) {
                new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, "no such mailbox").write(client);
                return;
            }

            sendMailboxStatus(client, command, mailbox);

            session.setState(ImapSessionState.SELECTED);
            session.setMailbox(mailbox);
        }
    }

}
