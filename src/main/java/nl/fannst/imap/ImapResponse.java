package nl.fannst.imap;

import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ImapResponse {
    public enum Type {
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

    public static class StatusCode {
        private static final char PREFIX = '[';
        private static final char SUFFIX = ']';

        public enum Type {
            ALERT("ALERT"),
            BAD_CHARSET("BADCHARSET"),
            CAPABILITY("CAPABILITY"),
            PARSE("PARSE"),
            PERMANENT_FLAGS("PERMANENTFLAGS"),
            READ_ONLY("READ-ONLY"),
            READ_WRITE("READ-WRITE"),
            TRY_CREATE("TRYCREATE"),
            UID_NEXT("UIDNEXT"),
            UID_VALIDITY("UIDVALIDITY"),
            UNSEEN("UNSEEN");

            private final String m_Keyword;

            Type(String keyword) {
                m_Keyword = keyword;
            }

            public String getKeyword() {
                return m_Keyword;
            }
        }

        private final Type m_Type;
        private final Object m_Argument;

        public StatusCode(Type type, Object arg) {
            m_Type = type;
            m_Argument = arg;
        }

        @Override
        public String toString() {
            if (m_Argument == null)
                return PREFIX + m_Type.getKeyword() + SUFFIX;

            return PREFIX + m_Type.getKeyword() + ' ' + m_Argument.toString() + SUFFIX;
        }
    }

    private final String m_Tag;
    private final Type m_Type;
    private final String m_Message;
    private final StatusCode m_StatusCode;
    private final Object m_Prefix;
    private final boolean m_Suffix;

    public ImapResponse(String tag, Type status, String message, boolean suffix, StatusCode statusCode, Object prefix) {
        m_Tag = tag;
        m_Type = status;
        m_Message = message;
        m_Suffix = suffix;
        m_StatusCode = statusCode;
        m_Prefix = prefix;
    }

    public ImapResponse(String tag, Type status, String message, StatusCode statusCode) {
        this(tag, status, message, true, statusCode, null);
    }

    public ImapResponse(String tag, Type status, String message, boolean suffix) {
        this(tag, status, message, suffix, null, null);
    }

    public ImapResponse(String tag, Type status, String message) {
        this(tag, status, message, true, null, null);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Adds the prefix, either tagged or non-tagged ( status response ).
        if (m_Tag == null)
            stringBuilder.append('*');
        else
            stringBuilder.append(m_Tag);

        // If there is any prefix, append the string version of
        //  it to the final result.
        if (m_Prefix != null) {
            stringBuilder.append(' ').append(m_Prefix.toString());
        }

        // If there is any type, append the keyword of it.
        if (m_Type != null) {
            stringBuilder.append(' ').append(m_Type.getKeyword());
        }

        // If there is any status code specified, add the status code to
        //  the final response.
        if (m_StatusCode != null) {
            stringBuilder.append(' ').append(m_StatusCode.toString());
        }

        // If there is an message, add it.
        if (m_Message != null) {
            stringBuilder.append(' ').append(m_Message);
        }

        if (m_Suffix) {
            stringBuilder.append(' ').append('-').append(' ').append("LukeIMAP");
        }

        stringBuilder.append("\r\n");

        return stringBuilder.toString();
    }

    public void write(NIOClientWrapperArgument client) throws IOException {
        client.write(toString());
    }
}
