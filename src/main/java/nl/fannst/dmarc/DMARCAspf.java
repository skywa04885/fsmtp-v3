package nl.fannst.dmarc;

public enum DMARCAspf {
    STRICT("s"),
    RELAXED("r");

    public static final String KEY = "v";

    private final String m_Keyword;

    DMARCAspf(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCAspf fromString(String raw) {
        for (DMARCAspf version : DMARCAspf.values()) {
            if (version.getKeyword().equalsIgnoreCase(raw)) {
                return version;
            }
        }

        return null;
    }
}
