package nl.fannst.imap;

import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;

public class ImapStatusResponse {
    public static enum Status {
        OK("OK"),
        NO("NO"),
        BAD("BAD"),
        PRE_AUTH("PREAUTH"),
        BYE("BYE");

        private final String m_Keyword;

        Status(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }

    private final Status m_Status;
    private final String m_Message;


    public ImapStatusResponse(Status status, String message) {
        m_Status = status;
        m_Message = message;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("* ")
                .append(m_Status.getKeyword())
                .append(' ')
                .append(m_Message)
                .append("\r\n");

        return stringBuilder.toString();
    }

    public void write(NIOClientWrapperArgument client) throws IOException {
        client.write(toString());
    }
}
