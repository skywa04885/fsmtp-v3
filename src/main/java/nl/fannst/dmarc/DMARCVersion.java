package nl.fannst.dmarc;

public enum DMARCVersion {
    DMARC1("dmarc1");

    public static final String KEY = "v";

    private final String m_Keyword;

    DMARCVersion(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCVersion fromString(String raw) {
        for (DMARCVersion version : DMARCVersion.values()) {
            if (version.getKeyword().equalsIgnoreCase(raw)) {
                return version;
            }
        }

        return null;
    }
}
