package nl.fannst.imap;

import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ImapResponse {
    public static enum Type {
        /* Status Responses */
        OK("OK"),
        NO("NO"),
        BAD("BAD"),
        PRE_AUTH("PREAUTH"),
        BYE("BYE"),
        /* Server and Mailbox status */
        CAPABILITY("CAPABILITY"),
        LIST("LIST"),
        L_SUB("LSUB"),
        STATUS("STATUS"),
        SEARCH("SEARCH"),
        FLAGS("FLAGS"),
        /* Mailbox Size */
        EXISTS("EXISTS"),
        RECENT("RECENT"),
        /* Message Status */
        EXPUNGE("EXPUNGE"),
        FETCH("FETCH");

        private final String m_Keyword;

        Type(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }

    private final String m_Tag;
    private final Type m_Type;
    private final String m_Message;

    public ImapResponse(String tag, Type status, String message) {
        m_Tag = tag;
        m_Type = status;
        m_Message = message;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Gets the hostname of the current machine, idk why..
        //  it just looks fancy in the telnet LOL!
        String hostname;
        try { hostname = InetAddress.getLocalHost().getHostName(); }
        catch (UnknownHostException ignore) { hostname = "fsmtp"; }

        // Adds the prefix, either tagged or non-tagged ( status response ).
        if (m_Tag == null)
            stringBuilder.append('*');
        else
            stringBuilder.append(m_Tag);

        if (m_Type != null)
            stringBuilder.append(' ').append(m_Type.getKeyword());

        // Adds the rest of the message.
        stringBuilder
                .append(' ')
                .append(m_Message)
                .append(' ')
                .append(hostname)
                .append("\r\n");

        return stringBuilder.toString();
    }

    public void write(NIOClientWrapperArgument client) throws IOException {
        client.write(toString());
    }
}
