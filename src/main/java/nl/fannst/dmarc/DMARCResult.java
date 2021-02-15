package nl.fannst.dmarc;

public enum DMARCResult {
    PASS("pass"),
    REJECTED("fail"),
    NEUTRAL("neutral"),
    QUARANTINE("quarantine");

    private final String m_Name;

    DMARCResult(String name) {
        m_Name = name;
    }

    public String getName() {
        return m_Name;
    }
}
