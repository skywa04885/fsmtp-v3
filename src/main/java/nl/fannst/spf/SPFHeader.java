package nl.fannst.spf;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SPFHeader {
    public static final String KEY = "x-fannst-spf";

    private final InetAddress m_ClientAddress;
    private final SPFResult m_Result;
    private final String m_Feedback;

    public SPFHeader(InetAddress clientAddress, SPFResult result, String feedback) {
        m_ClientAddress = clientAddress;
        m_Result = result;
        m_Feedback = feedback;
    }

    @Override
    public String toString() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return m_Result.getKeyword() + " (" + hostname + ": " + m_Feedback + ") client-ip=" + m_ClientAddress.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get hostname: " + e.getMessage());
        }
    }
}
