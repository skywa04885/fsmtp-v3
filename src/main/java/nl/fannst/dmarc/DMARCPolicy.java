package nl.fannst.dmarc;

public enum DMARCPolicy {
    NONE("none"),
    QUARANTINE("quarantine"),
    REJECT("reject");

    public static final String KEY = "p";

    private final String m_Keyword;

    DMARCPolicy(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }

    public static DMARCPolicy fromString(String raw) {
        for (DMARCPolicy policy : DMARCPolicy.values()) {
            if (policy.getKeyword().equalsIgnoreCase(raw)) {
                return policy;
            }
        }

        return null;
    }
}
