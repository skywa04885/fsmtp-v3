package nl.fannst.dmarc;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DMARCHeader {
    public static final String KEY = "x-fannst-dmarc";

    private final InetAddress m_ClientAddress;
    private final DMARCResult m_Result;
    private final String m_Feedback;

    public DMARCHeader(InetAddress clientAddress, DMARCResult result, String feedback) {
        m_ClientAddress = clientAddress;
        m_Result = result;
        m_Feedback = feedback;
    }

    @Override
    public String toString() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            return m_Result.getName() + " (" + hostname + ": " + m_Feedback + ") client-ip=" + m_ClientAddress.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get hostname: " + e.getMessage());
        }
    }
}
