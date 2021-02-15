package nl.fannst.dmarc;

import nl.fannst.Logger;
import nl.fannst.mime.Address;

import java.security.Policy;
import java.util.ArrayList;
import java.util.Locale;

public class DMARCRecord {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    /* Basic Header Information */
    private DMARCPolicy m_Policy;
    private DMARCPolicy m_SubdomainPolicy;
    private DMARCVersion m_Version;
    private DMARCAspf m_ASPF;
    private DMARCAdkim m_ADKIM;
    private int m_Percentage;

    /* Report Emails ( Not sure if this will allow extra features in future ) */
    private Address m_RUA;
    private Address m_RUF;

    /* Report Configuration */
    private DMARCFailureOptions m_FO;
    private DMARCReportFormat m_RF;
    private int m_RI;                       /* Report Interval */

    /****************************************************
     * Instance Methods
     ****************************************************/

    public void log(Logger logger) {
        logger.log("DMARCRecord {");
        logger.log("\t[General]");
        logger.log("\tPolicy: " + (m_Policy != null ? m_Policy.getKeyword() : "Null"));
        logger.log("\tSubdomainPolicy: " + (m_SubdomainPolicy != null ? m_SubdomainPolicy.getKeyword() : "Null"));
        logger.log("\tVersion: " + (m_Version != null ? m_Version.getKeyword() : "Null"));
        logger.log("\tASPF: " + (m_ASPF != null ? m_ASPF.getKeyword() : "Null"));
        logger.log("\tADMIN: " + (m_ADKIM != null ? m_ADKIM.getKeyword() : "Null"));
        logger.log("\tPass percentage: " + m_Percentage);
        logger.log("\t[Reports]");
        logger.log("\tRUA: " + (m_RUA != null ? m_RUA.toString() : "Don't send reports"));
        logger.log("\tRUF: " + (m_RUF != null ? m_RUF.toString() : "Don't send reports"));
        logger.log("\t[Report Configuration]");
        logger.log("\tFO: " + (m_FO != null ? m_FO.getKeyword() : "Null"));
        logger.log("\tRF: " + (m_RF != null ? m_RF.getKeyword() : "Null"));
        logger.log("\tRI: " + m_RI);
        logger.log("}");
    }

    /****************************************************
     * Parse Methods
     ****************************************************/

    /**
     * Parses an key value pair, and updates the specified record with the found values
     * @param raw the raw k/v pair
     * @param record the target record
     */
    private static void parseKVPair(String raw, DMARCRecord record) {
        int pos = raw.indexOf('=');

        String key = raw.substring(0, pos), value = raw.substring(pos + 1);
        switch (key.toLowerCase(Locale.ROOT)) {
            case "v":
                record.m_Version = DMARCVersion.fromString(value);
                break;
            case "p":
                record.m_Policy = DMARCPolicy.fromString(value);
                break;
            case "pct":
                try {
                    record.m_Percentage = Integer.parseInt(value);
                } catch (NumberFormatException ignore) {}
                break;
            case "fo":
                record.m_FO = DMARCFailureOptions.fromString(value);
                break;
            case "aspf":
                record.m_ASPF = DMARCAspf.fromString(value);
                break;
            case "adkim":
                record.m_ADKIM = DMARCAdkim.fromString(value);
                break;
            case "rf":
                record.m_RF = DMARCReportFormat.fromString(value);
                break;
            case "ri":
                try {
                    record.m_RI = Integer.parseInt(value);
                } catch (NumberFormatException ignore) {}
                break;
            case "sp":
                record.m_SubdomainPolicy = DMARCPolicy.fromString(value);
                break;
            case "rua": {
                if (value.indexOf(':') == -1) break;

                try {
                    record.m_RUA = Address.parse(value.substring(value.indexOf(':') + 1));
                } catch (Address.InvalidAddressException ignore) {}
            }
            case "ruf": {
                if (value.indexOf(':') == -1) break;

                try {
                    record.m_RUF = Address.parse(value.substring(value.indexOf(':') + 1));
                } catch (Address.InvalidAddressException ignore) {}
            }
        }
    }

    /**
     * Parses an raw DMARC record
     * @param raw the raw record
     * @return the result
     */
    public static DMARCRecord parse(String raw) {
        DMARCRecord record = new DMARCRecord();

        for (String segment : raw.split(";\\s+|;")) {
            parseKVPair(segment, record);
        }

        return record;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public DMARCPolicy getPolicy() {
        return m_Policy;
    }

    public DMARCPolicy getSubdomainPolicy() {
        return m_SubdomainPolicy;
    }

    public int getPercentage() {
        return m_Percentage;
    }

    public DMARCAspf getASPF() {
        return m_ASPF;
    }

    public DMARCAdkim getAdkim() {
        return m_ADKIM;
    }
}
