package nl.fannst.dmarc;

public enum DMARCAdkim {
    STRICT("s"),
    RELAXED("r");

    public static final String KEY = "v";

    private final String m_Keyword;

    DMARCAdkim(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCAdkim fromString(String raw) {
        for (DMARCAdkim version : DMARCAdkim.values()) {
            if (version.getKeyword().equalsIgnoreCase(raw)) {
                return version;
            }
        }

        return null;
    }
}
