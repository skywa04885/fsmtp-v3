package nl.fannst.imap.fancy_responses;

import nl.fannst.imap.ImapResponse;
import nl.fannst.imap.datatypes.ImapList;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ImapMailboxInfoResponse extends FancyImapResponse {
    private final int m_Exists;
    private final int m_Recent;
    private final int m_Unseen;
    private final Message.Flag[] m_Flags;
    private final int m_UIDNext;
    private final int m_UIDValidity;

    public ImapMailboxInfoResponse(int exists, int recent, int unseen, Message.Flag[] flags, int uidNext, int uidValidity) {
        m_Exists = exists;
        m_Recent = recent;
        m_Unseen = unseen;
        m_Flags = flags;
        m_UIDNext = uidNext;
        m_UIDValidity = uidValidity;
    }

    @Override
    public void write(NIOClientWrapperArgument client) throws IOException {
        //
        // Simple responses.
        //

        new ImapResponse(null, ImapResponse.Type.EXISTS, null, false, null, m_Exists).write(client);
        new ImapResponse(null, ImapResponse.Type.RECENT, null, false, null, m_Recent).write(client);
        new ImapResponse(null, ImapResponse.Type.FLAGS, new ImapList<Message.Flag>(Arrays.asList(m_Flags)).toString(),
                false, null, null).write(client);

        //
        // Tagged responses.
        //

        new ImapResponse(null, ImapResponse.Type.OK, "Messages unseen", false,
                new ImapResponse.StatusCode(ImapResponse.StatusCode.Type.UNSEEN, m_Unseen), null).write(client);

        new ImapResponse(null, ImapResponse.Type.OK, "is next UID", false,
                new ImapResponse.StatusCode(ImapResponse.StatusCode.Type.UID_NEXT, m_UIDNext), null).write(client);

        new ImapResponse(null, ImapResponse.Type.OK, "UIDs valid", false,
                new ImapResponse.StatusCode(ImapResponse.StatusCode.Type.UID_VALIDITY, m_UIDValidity), null).write(client);

        List<Message.Flag> permanentFlags = Arrays.stream(m_Flags).filter(Message.Flag::getPermanent).collect(Collectors.toList());
        new ImapResponse(null, ImapResponse.Type.OK, "Limited", false,
                new ImapResponse.StatusCode(ImapResponse.StatusCode.Type.PERMANENT_FLAGS, new ImapList<Message.Flag>(permanentFlags)), null).write(client);
    }
}
