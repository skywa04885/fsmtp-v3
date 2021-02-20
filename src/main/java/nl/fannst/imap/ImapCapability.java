package nl.fannst.imap;

public class ImapCapability {
    public static enum Type {
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

    public static enum AuthMechanism {
        X_OAUTH2("XOAUTH2"),
        PLAIN("PLAIN"),
        PLAIN_CLIENT_TOKEN("PLAIN-CLIENTTOKEN"),
        O_AUTH_BEARER("OAUTHBEARER"),
        X_OAUTH("XOAUTH");

        private final String m_Keyword;

        AuthMechanism(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }

    private final Type m_Type;
    private final String m_Arg;

    public ImapCapability(Type type, String arg) {
        m_Type = type;
        m_Arg = arg;
    }

    public ImapCapability(Type type) {
        this(type, null);
    }

    public ImapCapability(AuthMechanism mechanism) {
        this(Type.AUTH, mechanism.getKeyword());
    }

    @Override
    public String toString() {
        if (m_Arg == null) return m_Type.getKeyword();
        else return m_Type.getKeyword() + '=' + m_Arg;
    }
}
