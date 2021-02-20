package nl.fannst.imap.fancy_responses;

import nl.fannst.imap.ImapResponse;
import nl.fannst.models.mail.Message;
import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class ImapSelectResponse extends FancyImapResponse {
    private final int m_Exists;
    private final int m_Recent;
    private final int m_Unseen;
    private final Message.Flag[] m_Flags;
    private final int m_UIDNext;
    private final int m_UIDValidity;

    public ImapSelectResponse(int exists, int recent, int unseen, Message.Flag[] flags, int uidNext, int uidValidity) {
        m_Exists = exists;
        m_Recent = recent;
        m_Unseen = unseen;
        m_Flags = flags;
        m_UIDNext = uidNext;
        m_UIDValidity = uidValidity;
    }

    @Override
    public void write(NIOClientWrapperArgument client) throws IOException {
        new ImapResponse(null, null, m_Exists + " EXISTS").write(client);
        new ImapResponse(null, null, m_Recent + " RECENT").write(client);
        new ImapResponse(null, ImapResponse.Type.OK, "[UNSEEN " + m_Unseen + "]").write(client);
        new ImapResponse(null, ImapResponse.Type.OK, "[UIDNEXT " + m_UIDNext + "]").write(client);
        new ImapResponse(null, ImapResponse.Type.OK, "[UIDVALIDITY " + m_UIDValidity + "]").write(client);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("FLAGS").append(' ').append('(');

        for (int i = 0; i < m_Flags.length; ++i) {
            stringBuilder.append(m_Flags[i].toString());

            if (i + 1 < m_Flags.length)
                stringBuilder.append(' ');
        }

        stringBuilder.append(')');
    }
}
