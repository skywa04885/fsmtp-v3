package nl.fannst.dkim;

public enum DKIMResult {
    PASS("pass"),
    REJECTED("fail"),
    NEUTRAL("neutral");

    private final String m_Name;

    DKIMResult(String name) {
        m_Name = name;
    }

    public String getName() {
        return m_Name;
    }
}
