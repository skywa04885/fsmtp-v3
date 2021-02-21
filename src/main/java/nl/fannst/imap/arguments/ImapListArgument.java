package nl.fannst.imap.arguments;

import nl.fannst.Globals;
import nl.fannst.imap.ImapCommand;

public class ImapListArgument extends ImapCommandArgument {
    private final String m_ReferenceName;
    private final String m_MailboxName;

    /****************************************************
     * Classy Stuff
     ****************************************************/

    public ImapListArgument(String referenceName, String mailboxName) {
        m_ReferenceName = referenceName;
        m_MailboxName = mailboxName;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapListArgument parse(String raw) throws ImapCommand.SyntaxException {
        String[] segments = raw.split("\\s+");
        return null;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getReferenceName() {
        return m_ReferenceName;
    }

    public String getMailboxName() {
        return m_MailboxName;
    }
}
