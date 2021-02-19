package nl.fannst.imap;

public class ImapCommand {
    public static enum Type {
        /* Client Commands - Any State */
        CAPABILITY("CAPABILITY"),
        NOOP("NOOP"),
        LOGOUT("LOGOUT"),
        /* Client Commands - Not Authenticated State */
        STARTTLS("STARTTLS"),
        AUTHENTICATE("AUTHENTICATE"),
        /* Client Commands - Authenticated State */
        SELECT("SELECT"),
        EXAMINE("EXAMINE"),
        CREATE("CREATE"),
        DELETE("DELETE"),
        RENAME("RENAME"),
        SUBSCRIBE("SUBSCRIBE"),
        UNSUBSCRIBE("UNSUBSCRIBE"),
        LIST("LIST"),
        L_SUB("LSUB"),
        STATUS("STATUS"),
        APPEND("APPEND"),
        /* Client Commands - Selected State */
        CHECK("CHECK"),
        CLOSE("CLOSE"),
        EXPUNGE("EXPUNGE"),
        SEARCH("SEARCH"),
        FETCH("FETCH"),
        STORE("STORE"),
        COPY("COPY"),
        UID("UID"),
        /* Client Commands - Extended */
        X_AUTHOR("XAUTHOR");

        private final String m_Keyword;

        Type(String keyword) {
            m_Keyword = keyword;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_SequenceNo;
    private final Type m_Type;

    public ImapCommand(String sequenceNo, Type type) {
        m_SequenceNo = sequenceNo;
        m_Type = type;
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getSequenceNo() {
        return m_SequenceNo;
    }

    public Type getType() {
        return m_Type;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapCommand parse(String raw) {

    }
}
