package nl.fannst.dmarc;

import nl.fannst.Logger;
import nl.fannst.dkim.DKIMResult;
import nl.fannst.mime.Address;
import nl.fannst.net.DNS;
import nl.fannst.spf.SPFResult;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.Random;

public class DMARCValidator {
    private final Address m_From;
    private final Address m_MailFrom;
    private final Logger m_Logger;
    private String m_Feedback;

    public DMARCValidator(Address from, Address mailFrom) {
        m_From = from;
        m_MailFrom = mailFrom;
        m_Logger = new Logger("DMARCValidator", Logger.Level.TRACE);
    }

    public DMARCResult validate(SPFResult spfResult, DKIMResult dkimResult) {
        boolean hasSubdomain = m_From.hasSubdomain();
        String domain = hasSubdomain ? m_From.getWithoutSubdomain() : m_From.getDomain();

        //
        // Gets the DMARC Record
        //

        DMARCRecord dmarcRecord;
        try {
            if (Logger.allowTrace()) m_Logger.log("Resolving DMARC record for: " + domain);

            ArrayList<DMARCRecord> dmarcRecords = DNS.getDMARCRecords("_dmarc." + domain);

            if (dmarcRecords.size() == 0) {
                if (Logger.allowTrace()) {
                    m_Logger.log("No DMARC Records found", Logger.Level.WARN);
                }

                m_Feedback = "no record found.";
                return DMARCResult.NEUTRAL;
            }

            dmarcRecord = dmarcRecords.get(0);
        } catch (NamingException e) {
            if (Logger.allowTrace()) m_Logger.log("Failed to resolve domain: " + e.getMessage(), Logger.Level.ERROR);

            m_Feedback = "failed to resolve domain.";
            return DMARCResult.NEUTRAL;
        }

        if (Logger.allowTrace()) dmarcRecord.log(m_Logger);


        //
        // Validates ASPF & ADKIM
        //

        // Performs ASPF validation ( if specified )
        if (dmarcRecord.getASPF() != null) {
            DMARCAspf aspf = dmarcRecord.getASPF();

            if (aspf == DMARCAspf.RELAXED) {
                String a = m_From.hasSubdomain() ? m_From.getWithoutSubdomain() : m_From.getDomain();
                String b = m_MailFrom.hasSubdomain() ? m_MailFrom.getWithoutSubdomain() : m_MailFrom.getDomain();

                if (!a.equalsIgnoreCase(b)) {
                    m_Feedback = "mail-from and from do not have any matching domain";
                    return DMARCResult.REJECTED;
                }
            } else if (aspf == DMARCAspf.STRICT) {
                if (!m_From.getDomain().equalsIgnoreCase(m_MailFrom.getDomain())) {
                    m_Feedback = "mail-from and from do not have any matching domain";
                    return DMARCResult.REJECTED;
                }
            }
        }

        // Performs ADKIM validation ( if specified )
        if (dmarcRecord.getAdkim() != null) {
            DMARCAdkim adkim = dmarcRecord.getAdkim();
        }

        //
        // Checks if we should pass
        //

        // Checks if we're dealing with a subdomain or domain
        DMARCPolicy policy = hasSubdomain ? dmarcRecord.getSubdomainPolicy() : dmarcRecord.getPolicy();
        switch (policy) {
            case NONE:
                m_Feedback = "no policy specified";
                return DMARCResult.NEUTRAL;
            case REJECT:
            case QUARANTINE:{
                if (spfResult == SPFResult.Rejected && dkimResult == DKIMResult.REJECTED) {
                    if (new Random().nextInt(100) > dmarcRecord.getPercentage()) {
                        m_Feedback = "rejected, but passed percentage.";
                        return DMARCResult.PASS;
                    }

                    if (Logger.allowTrace()) m_Logger.log("SPF or DKIM failed, rejecting ..", Logger.Level.ERROR);
                    return policy == DMARCPolicy.REJECT ? DMARCResult.REJECTED : DMARCResult.QUARANTINE;
                }
            }
        }

        return DMARCResult.PASS;
    }

    public String getFeedback() {
        return m_Feedback;
    }
}
