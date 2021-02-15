package nl.fannst.smtp.server.session;

public enum SmtpSessionProtocol {
    SMTP("SMTP"),
    ESMTP("ESMTP"),
    ESMTP_SA("ESMPTSA"),
    ESMTP_A("ESMTPA");

    private final String m_Keyword;

    SmtpSessionProtocol(String keyword) {
        m_Keyword = keyword;
    }

    public String getKeyword() {
        return m_Keyword;
    }
}
