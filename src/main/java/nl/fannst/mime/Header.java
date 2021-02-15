package nl.fannst.mime;

import nl.fannst.datatypes.Pair;

import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.stream.StreamSupport;

public class Header {
    public static final int MAX_LENGTH = 98;
    public static final String SMALL_INDENT = "      ";
    public static final String LARGE_INDENT = "       ";

    /****************************************************
     * Data Types
     ****************************************************/

    public static class InvalidHeaderException extends Exception {
        public InvalidHeaderException(String m) {
            super(m);
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private String m_Key, m_Value;
    private int m_HashCode;

    public Header(String key, String value) {
        m_Key = key;
        m_Value = value;
        m_HashCode = key.hashCode();
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    @Override
    public String toString() {
        if ((m_Key.length() + ": ".length() + m_Value.length()) < MAX_LENGTH) {
            return m_Key + ": " + m_Value;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(m_Key).append(": ");

        int length = stringBuilder.length();

        String[] segments = m_Value.split(";\\s+|;");
        int i = 0;
        for (String segment : segments) {
            if (segment.length() < MAX_LENGTH && (length + segment.length()) < MAX_LENGTH) {
                stringBuilder.append(segment);
                length += segment.length();
            } else if (length + segment.length() < MAX_LENGTH) {
                stringBuilder.append(i == 0 ? "" : "\r\n" + SMALL_INDENT).append(segment);
                length = segment.length() + SMALL_INDENT.length();
            } else { // Segment does not fit on line, nor fits in one line
                int current = 0, left = segment.length();
                int j = 0;

                int leftOnCurrentLine = MAX_LENGTH - length;
                if (leftOnCurrentLine > 0 && i == 0) {
                    stringBuilder.append(segment.substring(current, leftOnCurrentLine));
                    current += leftOnCurrentLine;
                    left -= leftOnCurrentLine;
                    ++j;
                }

                int max = MAX_LENGTH - LARGE_INDENT.length();
                while (left > 0) {
                    stringBuilder.append(j == 0 ? "\r\n" + SMALL_INDENT : "\r\n" + LARGE_INDENT);

                    if (left > max) {
                        stringBuilder.append(segment.substring(current, current + max));

                        current += max;
                        length = max;
                        left -= max;
                    } else {
                        stringBuilder.append(segment.substring(current, current + left));
                        length = left;
                        current += left;
                        left -= left;
                    }

                    length += j == 0 ? SMALL_INDENT.length() : LARGE_INDENT.length();
                    ++j;
                }
            }

            if (++i < segments.length) {
                stringBuilder.append("; ");
                length += 2;
            }
        }

        return stringBuilder.toString();
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Parses an raw header
     * @param raw the raw header
     * @return the parsed header
     * @throws InvalidHeaderException possibly invalid header
     */
    public static Header parse(String raw) throws InvalidHeaderException {
        int pos = raw.indexOf(':');
        if (pos == -1) {
            throw new InvalidHeaderException("No separator found.");
        }

        // Prepares the key and value
        String key = raw.substring(0, pos).toLowerCase(Locale.ROOT).trim();
        String value = raw.substring(pos + 1).replaceAll("\\s+"," ").trim();

        // Returns the new header class with the prepared key and value
        return new Header(key, value);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getKey() {
        return m_Key;
    }

    public String getValue() {
        return m_Value;
    }

    public void setKey(String key) {
        m_Key = key;
        m_HashCode = key.hashCode();
    }

    public void setValue(String value) {
        m_Value = value;
    }

    public int getHashCode() {
        return m_HashCode;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Splits the headers and body of MIME message
     *
     * @param scanner the scanner
     * @return the headers and body ( headers, body )
     */
    public static Pair<String, String> splitHeadersAndBody(Scanner scanner) {
        StringBuilder headers = new StringBuilder(), body = new StringBuilder();

        boolean atBody = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (atBody) {
                body.append(line).append("\r\n");
            } else {
                if (line.trim().isEmpty()) {
                    atBody = true;
                    continue;
                }

                headers.append(line).append("\r\n");
            }
        }

        return new Pair<String, String>(headers.toString(), body.toString());
    }

    /**
     * Parses the headers from a MIME message
     *
     * @param scanner the scanner to read from
     * @return the parsed & joined headers
     * @throws Header.InvalidHeaderException possible invalid header
     */
    public static ArrayList<Header> parseHeaders(Scanner scanner) throws Header.InvalidHeaderException {
        ArrayList<Header> result = new ArrayList<Header>();

        ArrayList<String> rawHeaders = new ArrayList<String>();

        StringBuilder joinedLine = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) break;

            if (line.charAt(0) != ' ' && line.charAt(0) != '\t') {
                if (joinedLine.length() > 0) {
                    result.add(Header.parse(joinedLine.toString()));
                    joinedLine = new StringBuilder();
                }
            }

            joinedLine.append(line.replaceAll("\\s+", " ").trim());
        }

        if (joinedLine.length() > 0) {
            result.add(Header.parse(joinedLine.toString()));
        }

        // Returns the result headers
        return result;
    }

    public static String buildHeaders(ArrayList<Header> headers) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Header header : headers) {
            stringBuilder.append(header.toString()).append("\r\n");
        }

        return stringBuilder.toString();
    }
}
