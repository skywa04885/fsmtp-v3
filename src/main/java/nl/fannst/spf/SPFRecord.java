package nl.fannst.spf;

import nl.fannst.Logger;
import nl.fannst.net.ip.CIDR_IPv4Address;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Locale;

public class SPFRecord {
    /****************************************************
     * Static Stuff
     ****************************************************/

    public static final String SOFT_FAIL_POLICY_KEYWORD = "~all";
    public static final String HARD_FAIL_POLICY_KEYWORD = "-all";
    public static final String ALL_POLICY_KEYWORD = "+all";
    public static final String ALL_NO_VALIDATION_KEYWORD = "?all";

    public static final int MX_FLAG = (1);
    public static final int A_FLAG = (1 << 1);

    /****************************************************
     * Data Types
     ****************************************************/

    public static enum Policy {
        SOFT_FAIL(SOFT_FAIL_POLICY_KEYWORD, "Soft Fail"),
        HARD_FAIL(HARD_FAIL_POLICY_KEYWORD, "Hard Fail"),
        ALL(ALL_POLICY_KEYWORD, "Allow All"),
        ALL_NO_VALIDATION(ALL_NO_VALIDATION_KEYWORD, "No Extra Validation");

        private final String m_Keyword;
        private final String m_Name;

        Policy(String keyword, String name) {
            m_Keyword = keyword;
            m_Name = name;
        }

        public String getKeyword() {
            return m_Keyword;
        }

        public String getName() {
            return m_Name;
        }

        /**
         * Gets the policy from an specified keyword
         * @param keyword the keyword of policy
         * @return the policy, or null
         */
        public static Policy fromKeyword(String keyword) {
            for (Policy p : Policy.values()) {
                if (p.getKeyword().equalsIgnoreCase(keyword)) {
                    return p;
                }
            }

            return null;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private ArrayList<CIDR_IPv4Address> m_AllowedIPv4s;
    private ArrayList<String> m_Included;
    private ArrayList<String> m_PTRs;
    private String m_Redirect;
    private Policy m_Policy;
    private SPFVersion m_Ver;
    private int m_Flags;

    public SPFRecord() {
        m_Policy = Policy.ALL;
        m_Ver = SPFVersion.SPF1;

        m_AllowedIPv4s = new ArrayList<CIDR_IPv4Address>();
        m_Included = new ArrayList<String>();
        m_PTRs = new ArrayList<String>();
        m_Redirect = null;

        m_Flags = 0;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    private static void parseFlag(SPFRecord record, String flag) {
        switch (flag) {
            case "mx": case "+mx":
                record.setFlag(MX_FLAG);
                break;
            case "a": case "+a":
                record.setFlag(A_FLAG);
                break;
            case SOFT_FAIL_POLICY_KEYWORD:
                record.setPolicy(Policy.SOFT_FAIL);
                break;
            case HARD_FAIL_POLICY_KEYWORD:
                record.setPolicy(Policy.HARD_FAIL);
                break;
            case ALL_POLICY_KEYWORD:
                record.setPolicy(Policy.ALL);
                break;
            case ALL_NO_VALIDATION_KEYWORD:
                record.setPolicy(Policy.ALL_NO_VALIDATION);
                break;
        }
    }

    private static void parseKVPair(SPFRecord record, String pair) {
        int pos = pair.indexOf(':');
        if (pos == -1) pos = pair.indexOf('=');

        String key = pair.substring(0, pos), value = pair.substring(pos + 1);

        switch (key) {
            case "ip4":
                try {
                    record.addIPv4Address(CIDR_IPv4Address.parse(value));
                } catch (CIDR_IPv4Address.InvalidException e) {
                    e.printStackTrace();
                }
                break;
            case "include":
                record.addIncluded(value);
                break;
            case "redirect":
                record.setRedirect(value);
                break;
            case "ptr":
                record.addPTR(value);
                break;
            case "v":
                record.setVer(SPFVersion.fromKeyword(value));
                break;
        }
    }

    public static SPFRecord parse(String raw) {
        SPFRecord result = new SPFRecord();

        String[] segments = raw.toLowerCase(Locale.ROOT).split("\\s+");
        for (String segment : segments) {
            if (segment.indexOf(':') == -1 && segment.indexOf('=') == -1) parseFlag(result, segment);
            else parseKVPair(result, segment);
        }

        return result;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void setPolicy(Policy policy) {
        m_Policy = policy;
    }

    public void setFlag(int flag) {
        m_Flags |= flag;
    }

    public boolean isFlagSet(int flag) {
        return (m_Flags & flag) != 0;
    }

    public void addIPv4Address(CIDR_IPv4Address address) {
        m_AllowedIPv4s.add(address);
    }

    public ArrayList<CIDR_IPv4Address> getAllowedIPv4s() {
        return m_AllowedIPv4s;
    }

    public void addIncluded(String include) {
        m_Included.add(include);
    }

    public ArrayList<String> getIncluded() {
        return m_Included;
    }

    public void setRedirect(String redirect) {
        m_Redirect = redirect;
    }

    public void addPTR(String ptr) {
        m_PTRs.add(ptr);
    }

    public ArrayList<String> getPTRs() {
        return m_PTRs;
    }

    public void setVer(SPFVersion ver) {
        m_Ver = ver;
    }

    public String getRedirect() {
        return m_Redirect;
    }

    public boolean shouldRedirect() {
        return m_Redirect != null;
    }

    public Policy getPolicy() {
        return m_Policy;
    }

    public void log(Logger logger) {
        int i;

        logger.log("SPFRecord {");

        // Basic Info //
        logger.log("\tVersion: " + m_Ver.getKeyword());
        logger.log("\tPolicy: " + m_Policy.getKeyword() + " (" + m_Policy.getName() + ')');
        logger.log("\tRedirect: " + (m_Redirect == null ? "No" : m_Redirect));

        // Flags //
        logger.log("\tFlags: (" + Integer.toBinaryString(m_Flags) + ')');
        if (isFlagSet(MX_FLAG)) logger.log("\t\tAllow from MX");
        if (isFlagSet(A_FLAG)) logger.log("\t\tAllow from A");

        // Included //
        i = 0;
        logger.log("\tIncluded:");
        for (String included : m_Included) {
            logger.log("\t\t" + i++ + " -> " + included);
        }

        // IPv4 Addresses //
        i = 0;
        logger.log("\tAllowed IPv4s:");
        for (CIDR_IPv4Address address : m_AllowedIPv4s) {
            logger.log("\t\t" + i++ + " -> " + address.toString());
        }

        // PTRs //
        i = 0;
        logger.log("\tPTRs:");
        for (String ptr : m_PTRs) {
            logger.log("\t\t" + i++ + " -> " + ptr);
        }

        logger.log("}");
    }
}
