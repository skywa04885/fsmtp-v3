package nl.fannst.net;

import nl.fannst.dmarc.DMARCRecord;
import nl.fannst.dmarc.DMARCVersion;
import nl.fannst.net.ip.CIDR_IPv4Address;
import nl.fannst.spf.SPFRecord;
import nl.fannst.spf.SPFVersion;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.List;

public class DNS {
    public static class MXRecord implements Comparable<MXRecord> {
        private final String m_Value;
        private final int m_Priority;

        public MXRecord(String value, int priority) {
            m_Value = value;
            m_Priority = priority;
        }

        public String getValue() {
            return m_Value;
        }

        public int getPriority() {
            return m_Priority;
        }

        @Override
        public int compareTo(MXRecord o) {
            return o.getPriority() > m_Priority ? -1 : 1;
        }
    }

    /**
     * Gets the MX records from domain
     * @param domainName the domain name
     * @return the list of MX records
     */
    public static ArrayList<MXRecord> getMXRecords(String domainName) throws NamingException {
        ArrayList<MXRecord> records = new ArrayList<MXRecord>(1);

        // Gets the MX records
        InitialDirContext initialDirContext = new InitialDirContext();
        Attributes attributes = initialDirContext.getAttributes("dns:/" + domainName, new String[] { "MX" });
        Attribute mxAttribute = attributes.get("MX");

        // If there are no records found, just return the domain.
        if (mxAttribute == null) {
            records.add(new MXRecord(domainName, 1));
            return records;
        }

        // Creates the array of MX records, after which we parse the string
        //  into readable MX records.
        records.ensureCapacity(mxAttribute.size());
        for (int i = 0; i < mxAttribute.size(); ++i) {
            String[] segments = mxAttribute.get(i).toString().split("\\s+");
            records.add(new MXRecord(segments[1], Integer.parseInt(segments[0])));
        }

        return records;
    }

    public static ArrayList<DMARCRecord> getDMARCRecords(String domain) throws NamingException {
        ArrayList<DMARCRecord> records = new ArrayList<DMARCRecord>();

        // Gets the TXT/SPF records
        InitialDirContext initialDirContext = new InitialDirContext();
        Attributes attributes = initialDirContext.getAttributes("dns:/" + domain, new String[]{
                "TXT"
        });

        // Gets the attributes and loops over them
        Attribute txtAttribute = attributes.get("TXT");
        if (txtAttribute == null) {
            return records;
        }

        for (int i = 0; i < txtAttribute.size(); ++i) {
            String record = txtAttribute.get(i).toString();
            record = record.substring(1, record.length() - 1);

            // Checks if there is whitespace to be detected, if not just proceed
            //  else perform substring, and check if dmarc
            int pos;
            if ((pos = record.indexOf(';')) != -1) {
                // Gets the first segment from the record, and checks if it equals
                //  v=dmarc1, if so we know that we're dealing with a SPF record.
                String segment = record.substring(0, pos);
                if (!segment.equalsIgnoreCase(DMARCVersion.KEY + '=' + DMARCVersion.DMARC1.getKeyword())) continue;

                records.add(DMARCRecord.parse(record));
            }
        }

        // Returns the records
        return records;
    }

    public static ArrayList<SPFRecord> getSPFRecords(String domain) throws NamingException {
        ArrayList<SPFRecord> records = new ArrayList<SPFRecord>();

        // Gets the TXT/SPF records
        InitialDirContext initialDirContext = new InitialDirContext();
        Attributes attributes = initialDirContext.getAttributes("dns:/" + domain, new String[] {
                "TXT"
        });

        // Gets the attributes and loops over them
        Attribute txtAttribute = attributes.get("TXT");
        if (txtAttribute == null) {
            return records;
        }

        for (int i = 0; i < txtAttribute.size(); ++i) {
            String record = txtAttribute.get(i).toString();
            record = record.substring(1, record.length() - 1);

            // Checks if there is whitespace to be detected, if not just proceed
            //  else perform substring, and check if spf.
            int pos;
            if ((pos = record.indexOf(' ')) != -1) {
                // Gets the first segment from the record, and checks if it equals
                //  v=spf1, if so we know that we're dealing with a SPF record.
                String segment = record.substring(0, pos);
                if (!segment.equalsIgnoreCase(SPFVersion.KEY + '=' + SPFVersion.SPF1.getKeyword())) continue;

                records.add(SPFRecord.parse(record));
            }
        }

        // Returns the records
        return records;
    }

    public static ArrayList<CIDR_IPv4Address> getARecords(String domain) throws NamingException {
        ArrayList<CIDR_IPv4Address> records = new ArrayList<CIDR_IPv4Address>();

        // Gets the A records
        InitialDirContext initialDirContext = new InitialDirContext();
        Attributes attributes = initialDirContext.getAttributes("dns:/" + domain, new String[] {
                "A"
        });

        // Gets the A records, loops over them and builds the final result array
        Attribute attribute = attributes.get("A");
        if (attribute == null) {
            return records;
        }

        for (int i = 0; i < attribute.size(); ++i) {
            String record = attribute.get(i).toString();

            try {
                records.add(CIDR_IPv4Address.parse(record));
            } catch (CIDR_IPv4Address.InvalidException e) {
                e.printStackTrace();
            }
        }

        return records;
    }
}
