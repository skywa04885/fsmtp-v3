package nl.fannst.smtp.headers;

import nl.fannst.dkim.DKIMResult;
import nl.fannst.dmarc.DMARCResult;
import nl.fannst.spf.SPFResult;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AuthenticationResults {
    public static final String key = "x-fannst-auth";
    private final DKIMResult m_DKIMResult;
    private final SPFResult m_SPFResult;
    private final DMARCResult m_DMARCResult;
    private final String m_DKIMFeedback;
    private final String m_SPFFeedback;
    private final String m_DMARCFeedback;

    public AuthenticationResults(DKIMResult dkimResult, SPFResult spfResult, DMARCResult dmarcResult, String dkiMFeedback, String spfFeedback, String dmarcFeedback) {
        m_DKIMResult = dkimResult;
        m_SPFResult = spfResult;
        m_DMARCResult = dmarcResult;
        m_DKIMFeedback = dkiMFeedback;
        m_SPFFeedback = spfFeedback;
        m_DMARCFeedback = dmarcFeedback;
    }

    @Override
    public String toString() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(hostname).append("; ");
            stringBuilder.append("dkim=").append(m_DKIMResult.getName()).append(" (").append(m_DKIMFeedback).append("); ");
            stringBuilder.append("spf=").append(m_SPFResult.getKeyword()).append(" (").append(m_SPFFeedback).append("); ");
            stringBuilder.append("dmarc=").append(m_DMARCResult.getName()).append(" (").append(m_DMARCFeedback).append(");");

            return stringBuilder.toString();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get hostname: " + e.getMessage());
        }
    }
}
