package nl.fannst.imap.arguments;

import nl.fannst.imap.ImapCommand;
import nl.fannst.mime.Address;

public class ImapSelectArgument extends ImapCommandArgument {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Mailbox;

    public ImapSelectArgument(String mailbox) {
        m_Mailbox = mailbox;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapSelectArgument parse(String raw) throws ImapCommand.SyntaxException {
        String[] segments = raw.split("\\s+");
        if (segments.length < 1)
            throw new ImapCommand.SyntaxException("not enough arguments");
        else if (segments.length > 1)
            throw new ImapCommand.SyntaxException("too many arguments");

        return new ImapSelectArgument(segments[0]);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getMailbox() {
        return m_Mailbox;
    }
}
