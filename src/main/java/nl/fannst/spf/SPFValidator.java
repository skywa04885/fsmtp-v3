package nl.fannst.spf;

import nl.fannst.Logger;
import nl.fannst.net.DNS;
import nl.fannst.net.ip.CIDR_IPv4Address;
import nl.fannst.net.ip.CIDR_IPv6Address;

import javax.naming.NamingException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SPFValidator {
    /****************************************************
     * Data Types
     ****************************************************/

    private enum Method {
        IPv4, IPv6
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Domain;
    private final CIDR_IPv4Address m_IPv4Address;
    private final CIDR_IPv6Address m_IPv6Address;
    private final Method m_Method;
    private final Logger m_Logger;
    private String m_Feedback;

    public SPFValidator(String domain, CIDR_IPv4Address ipv4, CIDR_IPv6Address ipv6, Method method) {
        m_Domain = domain;
        m_IPv4Address = ipv4;
        m_IPv6Address = ipv6;
        m_Method = method;
        m_Logger = new Logger("SPFValidator:" + domain, Logger.Level.TRACE);
    }

    public SPFValidator(String domain, CIDR_IPv4Address ipv4) {
        this(domain, ipv4, null, Method.IPv4);
    }

    public SPFValidator(String domain, CIDR_IPv6Address ipv6) {
        this(domain, null, ipv6, Method.IPv6);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    public SPFResult validate() {
        return recursiveValidator(m_Domain);
    }

    private SPFResult recursiveValidator(String domain) {
        SPFRecord record;

        if (Logger.allowTrace()) {
            if (m_Method == Method.IPv4) {
                m_Logger.log("Checking SPF on '" + domain + "' for address IPv4 '" + m_IPv4Address.toString() + '\'');
            }
        }

        //
        // Gets the record & redirects ( if specified )
        //

        // Gets the DNS record
        try {
            // Gets all the SPF records which could be found inside the DNS server
            //  of the specified domain.
            ArrayList<SPFRecord> records = DNS.getSPFRecords(domain);
            if (records.size() == 0) {
                m_Feedback = "No SPF record found.";
                return SPFResult.Neutral;
            } else if (records.size() > 1) {
                m_Feedback = "RFC 7208 violation! check paragraph 3.2, Multiple DNS Records.";
                return SPFResult.Violation;
            }

            // Uses the first found record as SPF record.
            record = records.get(0);
        } catch (NamingException e) {
            m_Feedback = "Domain not found!";
            return SPFResult.Neutral;
        }

        if (Logger.allowTrace()) record.log(m_Logger);

        // Checks if we need to redirect, if so redirect from current method
        //  to specified domain.
        if (record.getRedirect() != null) {
            if (Logger.allowTrace()) {
                m_Logger.log("Redirect order detected, redirecting to: '" + record.getRedirect() + '\'');
            }

            return recursiveValidator(record.getRedirect());
        }

        //
        // Performs some initial checks
        //

        // If the policy tells us to allow any email, just return accepted.
        if (record.getPolicy() == SPFRecord.Policy.ALL) {
            return SPFResult.Accepted;
        }

        //
        // Performs checks
        //

        // Loops over the included domains, and if any of those validates
        //  our IP address, return accepted.
        for (String included : record.getIncluded()) {
            if (recursiveValidator(included) == SPFResult.Accepted) return SPFResult.Accepted;
        }

        // Performs the IPv4 checks
        if (m_Method == Method.IPv4 && record.getAllowedIPv4s().size() > 0) {
            if (verifyIPv4s(record.getAllowedIPv4s())) return SPFResult.Accepted;
        }

        // Performs the A record checks
        if (record.isFlagSet(SPFRecord.A_FLAG) && m_Method == Method.IPv4) {
            if (verifyARecords(domain)) return SPFResult.Accepted;
        }

        // Performs the MX record checks
        if (record.isFlagSet(SPFRecord.MX_FLAG)) {
            if (verifyMXRecordsIPv4(domain)) return SPFResult.Accepted;
        }

        // Performs the PTR record checks
        if (m_Method == Method.IPv4 && record.getPTRs().size() > 0) {
            if (verifyReverseLookupIPv4(record.getPTRs())) return SPFResult.Accepted;
        }

        if (record.getPolicy() == SPFRecord.Policy.HARD_FAIL) {
            m_Feedback = "rejected, strict policy";
            return SPFResult.Rejected;
        } else {
            m_Feedback = "no strict policy";
            return SPFResult.Neutral;
        }
    }

    /****************************************************
     * Validation Methods
     ****************************************************/

    /**
     * Verifies the PTR section of SPF records
     * @param allowedExtensions the allowed extensions
     * @return match ?
     */
    private boolean verifyReverseLookupIPv4(ArrayList<String> allowedExtensions) {
        try {
            // Performs the reverse lookup of the IPv4 address
            String reverseLookup = Inet4Address.getByAddress(m_IPv4Address.getAddress()).getHostName();;

            // Loops over the allowed extensions, and checks if any of them matches our requirements.
            for (String ptr : allowedExtensions) {
                if (ptr.length() > reverseLookup.length()) continue;

                String extension = reverseLookup.substring(reverseLookup.length() - ptr.length());
                if (extension.equalsIgnoreCase(ptr)) {
                    m_Feedback = "PTR extension match: " + extension;
                    return true;
                }
            }
        } catch (UnknownHostException ignored) {}

        return false;
    }

    /**
     * Verifies MX records for the requested address
     * @param domain the domain
     * @return match ?
     */
    private boolean verifyMXRecordsIPv4(String domain) {
        try {
            ArrayList<DNS.MXRecord> mxRecords = DNS.getMXRecords(domain);
            for (DNS.MXRecord mxRecord : mxRecords) {
                try {
                    CIDR_IPv4Address mxAddress = new CIDR_IPv4Address(Inet4Address.getByName(mxRecord.getValue()).getAddress(), (byte) -1);

                    if (m_IPv4Address.equals(mxAddress)) {
                        m_Feedback = m_IPv4Address.toString() + " listed in MX record: " + mxRecord.getValue();
                        return true;
                    }
                } catch (UnknownHostException e) {
                    if (!Logger.allowTrace()) continue;

                    m_Logger.log("Failed to resolve " + mxRecord.getValue() +  " to an address.", Logger.Level.ERROR);
                }
            }
        } catch (NamingException e) {
            if (Logger.allowTrace()) {
                m_Logger.log("Failed to resolve MX records: " + e.getMessage(), Logger.Level.ERROR);
            }
        }

        return false;
    }

    /**
     * Verifies A records for the matching address
     * @param domain the domain to check
     * @return was there any match
     */
    private boolean verifyARecords(String domain) {
        try {
            ArrayList<CIDR_IPv4Address> aRecords = DNS.getARecords(domain);

            if (Logger.allowTrace()) {
                m_Logger.log("Validating " + aRecords.size() + " A Records ...");
            }

            for (CIDR_IPv4Address address : aRecords) {
                if (address.equals(m_IPv4Address)) {
                    m_Feedback = m_IPv4Address.toString() + " listed in A records";
                    return true;
                }
            }
        } catch (NamingException e) {
            if (Logger.allowTrace()) {
                m_Logger.log("Failed to resolve A records: " + e.getMessage(), Logger.Level.ERROR);
            }
        }

        return false;
    }

    /**
     * Verifies IPv4 addresses for matches
     * @param ipv4s the allowed IPv4s
     * @return
     */
    private boolean verifyIPv4s(ArrayList<CIDR_IPv4Address> ipv4s) {
        if (Logger.allowTrace()) {
            m_Logger.log("Validating " + ipv4s.size() + " IPv4 addresses/ranges ...");
        }

        for (CIDR_IPv4Address address : ipv4s) {
            if (address.getRange() == -1 && m_IPv4Address.equals(address)) {
                m_Feedback = m_IPv4Address + " listed in SPF IPv4s";
                return true;
            } else if (address.getRange() != -1 && m_IPv4Address.inSubnet(address)) {
                m_Feedback = m_IPv4Address + " part of IPv4 subnet " + address.toString();
                return true;
            }
        }

        return false;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getFeedback() {
        return m_Feedback;
    }
}
