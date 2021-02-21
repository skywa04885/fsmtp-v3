package nl.fannst.imap.server.commands.imap;

import nl.fannst.imap.ImapCommand;
import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.arguments.ImapStatusArgument;
import nl.fannst.imap.datatypes.ImapList;
import nl.fannst.imap.server.commands.ImapCommandHandler;
import nl.fannst.imap.server.commands.ImapCommandRequirement;
import nl.fannst.imap.server.session.ImapSession;
import nl.fannst.models.accounts.BasicAccount;
import nl.fannst.models.mail.Mailbox;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StatusCommand implements ImapCommandHandler {
    private static final String MAILBOX_DOESNT_EXISTS_MESSAGE = "status failure: no status for that name";
    private static final String STATUS_COMPLETED_MESSAGE = "status completed";

    @Override
    public void allowed(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapCommandRequirement.requireAuthenticated(client, command);
    }

    @Override
    public void handle(NIOClientWrapperArgument client, ImapCommand command) throws Exception {
        ImapSession session = (ImapSession) client.getClientWrapper().attachment();
        ImapStatusArgument argument = (ImapStatusArgument) command.getArgument();

        // Gets the mailbox by the specified name, if not found send error.
        Mailbox mailbox = Mailbox.getByName(session.getAccount().getUUID(), argument.getMailbox());
        if (mailbox == null) {
            new ImapResponse(command.getSequenceNo(), ImapResponse.Type.NO, MAILBOX_DOESNT_EXISTS_MESSAGE).write(client);
            return;
        }

        // Builds the result list of status items, this will be joined as an Imap list
        //  when sending it to the client.
        ArrayList<String> statusItems = new ArrayList<String>();
        for (ImapStatusArgument.Item item : argument.getRequestedItems()) {
            statusItems.add(item.getKeyword());

            switch (item) {
                case RECENT -> {
                    List<Integer> recentUIDs = Message.getRecent(session.getAccount().getUUID(), mailbox.getMailboxID());
                    statusItems.add(Integer.toString(recentUIDs.size()));
                }
                case UNSEEN -> {
                    List<Integer> unseenUIDs = Message.getUIDsWhereFlagClear(session.getAccount().getUUID(), mailbox.getMailboxID(),
                            Message.Flag.SEEN.getMask());
                    statusItems.add(Integer.toString(unseenUIDs.size()));
                }
                case MESSAGES -> statusItems.add(Integer.toString(mailbox.getMessageCount()));
                case UID_NEXT -> statusItems.add(Integer.toString(Objects.requireNonNull(
                        BasicAccount.getNextUid(session.getAccount().getUUID()))));
                case UID_VALIDITY -> statusItems.add(Integer.toString(Integer.MAX_VALUE));
            }
        }

        // Sends the response to the client.
        new ImapResponse(null, ImapResponse.Type.STATUS,
                new ImapList<String>(statusItems).toString(), false).write(client);
        new ImapResponse(command.getSequenceNo(), ImapResponse.Type.OK, STATUS_COMPLETED_MESSAGE).write(client);
    }
}
