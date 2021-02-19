package nl.fannst.imap;

public class ImapCapability {
    public enum Type {
        IMAP4rev1("IMAP4rev1"),
        AUTH("AUTH"),
        STARTTLS("STARTTLS"),
        LOGIN_DISABLED("LOGINDISABLED");

        private final String m_Keyword;

        Type(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }
}
