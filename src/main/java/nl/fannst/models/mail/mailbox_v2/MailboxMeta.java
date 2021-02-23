package nl.fannst.models.mail.mailbox_v2;

import org.bson.Document;

import javax.print.Doc;

public class MailboxMeta {
    /****************************************************
     * Data Types
     ****************************************************/

    public enum SystemFlags {
        INCOMING(0, "Incoming"),
        OUTGOING(1, "Outgoing"),
        SUSPICIOUS(2, "Suspicious"),
        SYSTEM(3, "System"),
        SOLID(4, "Solid"),
        READ_ONLY(5, "Read-Only");

        private final int m_Mask;
        private final String m_Keyword;

        SystemFlags(int bit, String keyword) {
            m_Mask = (1 << bit);
            m_Keyword = keyword;
        }

        public int getMask() {
            return m_Mask;
        }

        public String getKeyword() {
            return m_Keyword;
        }
    }

    public enum ImapFlags {
        ALL(0, "All"),
        ARCHIVE(1, "Archive"),
        DRAFTS(2, "Drafts"),
        FLAGGED(3, "Flagged"),
        JUNK(4, "Junk"),
        SENT(5, "Sent"),
        TRASH(6, "Trash"),
        MARKED(7, "Marked"),
        HAS_CHILDREN(8, "HasChildren"),
        HAS_NO_CHILDREN(9, "HasNoChildren");

        private final int m_Mask;
        private final String m_Keyword;

        ImapFlags(int bit, String keyword) {
            m_Mask = (1 << bit);
            m_Keyword = keyword;
        }

        public int getMask() {
            return m_Mask;
        }

        public String getKeyword() {
            return m_Keyword;
        }

        @Override
        public String toString() {
            return '/' + m_Keyword;
        }
    }

    /****************************************************
     * Static Variables
     ****************************************************/

    private static final String MESSAGE_COUNT_FIELD = "c";
    private static final String SYSTEM_FLAGS_FIELD = "s_f";
    private static final String IMAP_FLAGS_FIELD = "i_f";
    private static final String TOTAL_SIZE_FIELD = "ts";

    private static final int MESSAGE_COUNT_CHANGE_BIT = (1);
    private static final int SYSTEM_FLAGS_CHANGE_BIT = (1 << 1);
    private static final int IMAP_FLAGS_CHANGE_BIT = (1 << 2);
    private static final int TOTAL_SIZE_CHANGE_BIT = (1 << 3);

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private int m_MessageCount;
    private int m_TotalSize;
    private int m_SystemFlags;
    private int m_ImapFlags;
    private int m_ChangeBits;

    public MailboxMeta(int messageCount, int systemFlags, int imapFlags, int totalSize) {
        m_MessageCount = messageCount;
        m_SystemFlags = systemFlags;
        m_ImapFlags = imapFlags;
        m_TotalSize = totalSize;
        m_ChangeBits = 0;
    }

    public MailboxMeta() {
        this(0, 0, 0, 0);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Creates the document version of the document.
     * @return the document version.
     */
    public Document toDocument() {
        Document document = new Document();

        document.append(MESSAGE_COUNT_FIELD, m_MessageCount);
        document.append(SYSTEM_FLAGS_FIELD, m_SystemFlags);
        document.append(TOTAL_SIZE_FIELD, m_TotalSize);
        document.append(IMAP_FLAGS_FIELD, m_ImapFlags);

        return document;
    }

    public Document toUpdateDocument() {
        Document document = new Document();

        if ((m_ChangeBits & MESSAGE_COUNT_CHANGE_BIT) != 0)
            document.append(MESSAGE_COUNT_FIELD, m_MessageCount);
        if ((m_ChangeBits & SYSTEM_FLAGS_CHANGE_BIT) != 0)
            document.append(SYSTEM_FLAGS_FIELD, m_SystemFlags);
        if ((m_ChangeBits & IMAP_FLAGS_CHANGE_BIT) != 0)
            document.append(IMAP_FLAGS_FIELD, m_ImapFlags);
        if ((m_ChangeBits & TOTAL_SIZE_CHANGE_BIT) != 0)
            document.append(TOTAL_SIZE_FIELD, m_TotalSize);

        return document;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void setImapFlag(ImapFlags flag) {
        m_ChangeBits |= IMAP_FLAGS_CHANGE_BIT;
        m_ImapFlags |= flag.getMask();
    }

    public void clearImapFlag(ImapFlags flag) {
        m_ChangeBits |= IMAP_FLAGS_CHANGE_BIT;
        m_ImapFlags &= ~flag.getMask();
    }

    public void incrementMessageCount() {
        m_ChangeBits |= MESSAGE_COUNT_CHANGE_BIT;
        ++m_MessageCount;
    }

    public boolean isSystemFlagSet(SystemFlags flag) {
        return (m_SystemFlags & flag.getMask()) != 0;
    }

    public boolean isSystemFlagClear(SystemFlags flag) {
        return (m_SystemFlags & flag.getMask()) == 0;
    }

    public boolean isImapFlagSet(ImapFlags flag) {
        return (m_ImapFlags & flag.getMask()) != 0;
    }

    public boolean isImapFlagClear(ImapFlags flag) {
        return (m_ImapFlags & flag.getMask()) == 0;
    }

    public static MailboxMeta fromDocument(Document document) {
        return new MailboxMeta(document.getInteger(MESSAGE_COUNT_FIELD),
                document.getInteger(SYSTEM_FLAGS_FIELD),
                document.getInteger(IMAP_FLAGS_FIELD),
                document.getInteger(TOTAL_SIZE_FIELD));
    }
}
