package nl.fannst.spf;

public enum SPFVersion {
    SPF1("spf1");

    public static final String KEY = "v";

    private final String m_Keyword;

    SPFVersion(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static SPFVersion fromKeyword(String keyword) {
        for (SPFVersion v : SPFVersion.values()) {
            if (v.getKeyword().equalsIgnoreCase(keyword)) {
                return v;
            }
        }

        return null;
    }
}
