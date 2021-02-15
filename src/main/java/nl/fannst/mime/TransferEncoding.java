package nl.fannst.mime;

import java.util.Locale;

public enum TransferEncoding {
    EIGHT_BIT("8bit"),
    SEVEN_BIT("7bit"),
    QUOTED_PRINTABLE("quoted-printable"),
    BASE64("base64");

    private final String m_Keyword;
    private final int m_HashCode;

    TransferEncoding(String keyword) {
        m_Keyword = keyword;
        m_HashCode = keyword.hashCode();
    }

    public String getKeyWord() {
        return m_Keyword;
    }

    public int getHashCode() {
        return m_HashCode;
    }

    /**
     * Gets an transfer encoding by keyword
     * @param keyword the keyword
     * @return the transfer encoding, or null
     */
    public static TransferEncoding getByTypeString(String keyword) {
        int hashCode = keyword.toLowerCase(Locale.ROOT).hashCode();

        for (TransferEncoding transferEncoding : TransferEncoding.values()) {
            if (transferEncoding.getHashCode() == hashCode) {
                return transferEncoding;
            }
        }

        return null;
    }
}
