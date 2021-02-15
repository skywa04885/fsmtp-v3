package nl.fannst.pop3;

import nl.fannst.net.NIOClientWrapperArgument;
import nl.fannst.net.plain.PlainNIOClientArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PopReply {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum Indicator {
        OK("+OK"),
        ERR("-ERR");

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final String m_Keyword;

        Indicator(String keyword) {
            m_Keyword = keyword;
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public String getKeyword() {
            return m_Keyword;
        }
    }

    public static enum ResponseCode {
        LOGIN_DELAY("LOGIN-DELAY"),
        IN_USE("IN-USE"),
        SYS_PERM("SYS/PERM"),
        SYS_TEMP("SYS/TEMP"),
        AUTH("AUTH"),
        UTF8("UTF8");

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final String m_Keyword;

        ResponseCode(String keyword) {
            m_Keyword = keyword;
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public String getKeyword() {
            return m_Keyword;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Indicator m_Indicator;
    private final ResponseCode m_ResponseCode;
    private final String m_Message;

    public PopReply(Indicator indicator, ResponseCode code, String message) {
        m_Indicator = indicator;
        m_ResponseCode = code;
        m_Message = message;
    }

    public PopReply(Indicator indicator, String message) {
        this(indicator, null, message);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Creates string version of reply
     * @return the string version
     */
    @Override
    public String toString() {
        if (m_ResponseCode == null) {
            return m_Indicator.getKeyword() + ' ' + m_Message;
        }

        return m_Indicator.getKeyword() + " [" + m_ResponseCode + "] " + m_Message;
    }

    /**
     * Writes the reply to the client
     * @param client the client
     * @throws IOException possible exception
     */
    public void write(NIOClientWrapperArgument client) throws IOException {
        String stringVersion = toString() + "\r\n";

        ByteBuffer byteBuffer = ByteBuffer.wrap(stringVersion.getBytes());
        client.write(byteBuffer);
    }

    /**
     * Writes an multiple line response to the client
     * @param lines the lines
     * @param client the client
     * @throws IOException possible IOException
     */
    public static void writeMultiline(ArrayList<String> lines, NIOClientWrapperArgument client) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        lines.forEach(line -> stringBuilder.append(line).append("\r\n"));
        stringBuilder.append(".\r\n");

        ByteBuffer byteBuffer = ByteBuffer.wrap(stringBuilder.toString().getBytes());
        client.write(byteBuffer);
    }
}
