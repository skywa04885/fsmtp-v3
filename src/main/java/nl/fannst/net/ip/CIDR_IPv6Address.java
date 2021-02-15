package nl.fannst.net.ip;

public class CIDR_IPv6Address {
    int[] m_Address;
    int m_Range;

    public CIDR_IPv6Address(int[] address, int range) {
        assert address.length == 16 : "IPv6 address must be 16 bytes!";

        m_Address = address;
        m_Range = range;
    }
}
