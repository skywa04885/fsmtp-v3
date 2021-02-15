package nl.fannst.smtp.server.runnables;

import nl.fannst.smtp.server.SmtpSessionProtocol;

import java.net.InetAddress;

public class SmtpSessionInfo {
    private final String m_GreetingHostname;
    private final InetAddress m_RemoteAddress;
    private final SmtpSessionProtocol m_SessionProtocol;
    private final byte m_Flags;

    public SmtpSessionInfo(String greetingHostname, InetAddress remoteAddress, SmtpSessionProtocol protocol, byte flags) {
        m_GreetingHostname = greetingHostname;
        m_RemoteAddress = remoteAddress;
        m_SessionProtocol = protocol;
        m_Flags = flags;
    }

    public String getGreetingHostname() {
        return m_GreetingHostname;
    }

    public InetAddress getRemoteAddress() {
        return m_RemoteAddress;
    }

    public SmtpSessionProtocol getSessionProtocol() {
        return m_SessionProtocol;
    }

    public byte getFlags() {
        return m_Flags;
    }
}
