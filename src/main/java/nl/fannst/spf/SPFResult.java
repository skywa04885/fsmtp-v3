package nl.fannst.spf;

public enum SPFResult {
    Rejected("rejected"),
    Accepted("pass"),
    Neutral("neutral"),
    Violation("violation");

    private final String m_Keyword;

    SPFResult(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }
}
