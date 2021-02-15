package nl.fannst.net.ip;

import java.net.Inet4Address;
import java.util.Arrays;

public class CIDR_IPv4Address {
    public static class InvalidException extends Exception {
        public InvalidException(String m) {
            super(m);
        }
    }

    private byte[] m_Address;
    private byte m_Range;

    public CIDR_IPv4Address(byte[] address, byte range) {
        assert address.length == 4 : "IPv4 Address length MUST be 4!";

        m_Address = address;
        m_Range = range;
    }

    public byte[] getAddress() {
        return m_Address;
    }

    public byte getRange() {
        return m_Range;
    }

    public void setAddress(byte[] address) {
        assert address.length == 4 : "IPv4 Address length MUST be 4!";

        m_Address = address;
    }

    public void setRange(byte range) {
        m_Range = range;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(m_Address[0] & 0xFF).append('.');
        stringBuilder.append(m_Address[1] & 0xFF).append('.');
        stringBuilder.append(m_Address[2] & 0xFF).append('.');
        stringBuilder.append(m_Address[3] & 0xFF);

        if (m_Range != -1) {
            stringBuilder.append('/').append(m_Range);
        }

        return stringBuilder.toString();
    }

    public boolean equals(CIDR_IPv4Address address) {
        return Arrays.equals(m_Address, address.getAddress()) && m_Range == address.getRange();
    }

    public boolean inSubnet(CIDR_IPv4Address subnet) {
        int address_a = binary();
        int address_b = subnet.binary();

        address_a >>= subnet.getRange();
        address_b >>= subnet.getRange();

        return (address_a == address_b);
    }

    public int binary() {
        return ((m_Address[0] & 0xFF) << 24) | ((m_Address[1] & 0xFF) << 16) | ((m_Address[2] & 0xFF) << 8) | (m_Address[3] & 0xFF);
    }

    public static CIDR_IPv4Address parse(String raw) throws InvalidException {
        try {
            String[] addressSegments;
            byte range;

            // Checks if an CIDR range is specified, if so parse it and use the other
            //  pieces for the address, else just put the range as -1, and use all
            //  the segments for address.
            if (raw.indexOf('/') == -1) {
                addressSegments = raw.split("\\.");
                range = -1;
            } else {
                addressSegments = raw.substring(0, raw.indexOf('/')).split("\\.");
                range = (byte) Integer.parseInt(raw.substring(raw.indexOf('/') + 1));
            }

            // Makes sure it is an valid IPv4 address
            if (addressSegments.length != 4) {
                throw new InvalidException("IPv4 address may not have more than 4 bytes.");
            }

            // Loops over the segments, and stores the bytes as part
            //  of the result address.
            byte[] address = new byte[4];
            for (int i = 0; i < 4; ++i) {
                address[i] = (byte) Integer.parseInt(addressSegments[i]);
            }

            // Returns the result address.
            return new CIDR_IPv4Address(address, range);
        } catch (NumberFormatException e) {
            throw new InvalidException("Invalid byte specified in address.");
        }
    }
}
