package nl.fannst.mime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Address {
    public static class InvalidAddressException extends Exception {
        public InvalidAddressException(String message) {
            super(message);
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private String m_Address, m_Name;

    /**
     * Creates a new address instance
     * @param address the address
     * @param name the name
     */
    public Address(String address, String name) {
        m_Address = address;
        m_Name = name;
    }

    /**
     * Creates new empty address instance
     */
    public Address() {
        this(null, null);
    }

    /**
     * Copies existing address
     * @param address the existing address
     */
    public Address(Address address) {
        this(address.getAddress(), address.getName());
    }

    /**
     * Parses an address from raw address
     * @param raw the raw address
     * @throws InvalidAddressException possibly invalid
     */
    public Address(String raw) throws InvalidAddressException {
        this(parse(raw));
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Override method to turn address into string
     * @return the string version
     */
    @Override
    public String toString() {
        if (m_Name == null) {
            return '<' + m_Address + '>';
        } else if (m_Name.indexOf('"') != -1) {
            return '"' + m_Name + "\" <" + m_Address + '>';
        } else {
            return m_Name + " <" + m_Address + '>';
        }
    }

    /**
     * Gets the domain from the instance address
     * @return the domain
     */
    public String getDomain() {
        assert (m_Address != null);

        int pos = m_Address.indexOf('@');
        assert (pos != -1);

        return m_Address.substring(pos + 1);
    }

    /**
     * Gets the username from instance address
     * @return the username
     */
    public String getUsername() {
        assert (m_Address != null);

        int pos = m_Address.indexOf('@');
        assert (pos != -1);

        return m_Address.substring(0, pos);
    }

    /**
     * Gets the domain without subdomain ( if there )
     * @return the domain
     */
    public String getWithoutSubdomain() {
        String domain = getDomain();
        return domain.substring(domain.lastIndexOf('.', domain.lastIndexOf('.') - 1) + 1);
    }

    public boolean hasSubdomain() {
        int occurrences = 0;

        for (char c : getDomain().toCharArray()) {
            if (c == '.') ++occurrences;
        }

        return occurrences > 1;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Parses an raw address int oclass
     * @param raw the raw address
     * @return the parsed address
     * @throws InvalidAddressException possibly invalid
     */
    public static Address parse(String raw) throws InvalidAddressException {
        Address address = new Address();
        int end;

        // Reduces the extra whitespace in the string
        raw = raw.replaceAll("\\s+", " ").trim();

        // Parses the address, we also support the '"' version, so thats
        //  why we perform certain checks
        if (raw.indexOf('<') != -1 && raw.indexOf('>') != -1) {
            if (raw.indexOf('"') == -1) {
                address.setName(raw.substring(0, raw.indexOf('<')).trim());
                address.setAddress(raw.substring(raw.indexOf('<') + 1, raw.indexOf('>')).trim());
            } else {
                address.setName(raw.substring(raw.indexOf('"') + 1, raw.lastIndexOf('"')).trim());
                address.setAddress(raw.substring(raw.indexOf('<', raw.indexOf('"')) + 1, raw.indexOf('>')).trim());
            }
        } else if (raw.indexOf('<') != -1 || raw.indexOf('>') != -1) {
            throw new InvalidAddressException("Opening or closing bracket missing!");
        } else {
            address.setAddress(raw);
        }

        if (address.getName() != null && address.getName().trim().length() == 0) {
            address.setName(null);
        }

        return address;
    }

    public static String buildAddressList(ArrayList<Address> addresses) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < addresses.size(); ++i) {
            result.append(addresses.get(i).toString());
            if (i + 1 != addresses.size()) result.append(", ");
        }

        return result.toString();
    }

    /**
     * Parses an list of addresses
     * @param raw the raw list
     * @return the list of addresses
     * @throws InvalidAddressException possible syntax error
     */
    public static ArrayList<Address> parseAddressList(String raw) throws InvalidAddressException
    {
        ArrayList<Address> result = new ArrayList<Address>();
        boolean escape = false;
        int start = 0, end = 0;

        // Starts looping over the chars, and separating all of the
        //  different addresses in the list
        for (char c : raw.toCharArray()) {
            if (c == '"' && !escape) {
                escape = true;
            } else if (c == '"') {
                escape = false;
            }

            if (c == ',' && !escape) {
                result.add(Address.parse(raw.substring(start, end)));
                start = end;
            }

            ++end;
        }

        // Checks if there is anything left, if the last char of raw
        //  is comma, ignore.. False alarm
        if (start != end && raw.charAt(raw.length() - 1) != ',') {
            result.add(Address.parse(raw.substring(start, end)));
        }

        return result;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void setAddress(String address) {
        m_Address = address;
    }

    public void setName(String name) {
        m_Name = name;
    }

    public String getAddress() {
        return m_Address;
    }

    public String getName() {
        return m_Name;
    }
}
