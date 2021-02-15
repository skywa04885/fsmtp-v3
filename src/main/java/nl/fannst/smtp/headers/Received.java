package nl.fannst.smtp.headers;

import nl.fannst.Globals;
import nl.fannst.mime.Address;
import nl.fannst.smtp.server.SmtpSessionProtocol;

import java.net.InetAddress;
import java.util.Date;

public class Received {
    public static final String KEY = "received";

    private final InetAddress m_From;
    private final String m_FromButNotSure;
    private final SmtpSessionProtocol m_Protocol;
    private final Date m_Date;

    public Received(InetAddress from, String fromButNotSure, SmtpSessionProtocol protocol, Date date) {
        m_From = from;
        m_FromButNotSure = fromButNotSure;
        m_Protocol = protocol;
        m_Date = date;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        // Creates the from field
        stringBuilder.append("from").append(' ').append(m_FromButNotSure).append(" (").append(m_From.getHostName())
                .append(" [").append(m_From.getHostAddress()).append(']').append(") ");

        // Creates the by field
        stringBuilder.append("by ").append(Globals.HOSTNAME).append(" with ").append(m_Protocol.getKeyword());

        // Adds the date
        stringBuilder.append("; ").append(Globals.MIME_DATE_FORMAT.format(m_Date));
        return stringBuilder.toString();
    }
}
