package nl.fannst.mime;

import java.util.Locale;

public enum MIMEVersion {
    M1_0("1.0");

    private final String m_Keyword;
    private final int m_HashCode;

    MIMEVersion(String keyword) {
        m_Keyword = keyword;
        m_HashCode = keyword.hashCode();
    }

    public String getKeyWord() {
        return m_Keyword;
    }

    public int getHashCode() {
        return m_HashCode;
    }

    public static MIMEVersion getByTypeString(String keyword) {
        int hashCode = keyword.toLowerCase(Locale.ROOT).hashCode();

        for (MIMEVersion transferEncoding : MIMEVersion.values()) {
            if (transferEncoding.getHashCode() == hashCode) {
                return transferEncoding;
            }
        }

        return null;
    }
}
