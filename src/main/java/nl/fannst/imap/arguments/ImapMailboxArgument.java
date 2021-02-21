package nl.fannst.imap.arguments;

import nl.fannst.Globals;
import nl.fannst.imap.ImapCommand;

import java.util.regex.Pattern;

public class ImapMailboxArgument extends ImapCommandArgument {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Mailbox;

    public ImapMailboxArgument(String mailbox) {
        m_Mailbox = mailbox;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapMailboxArgument parse(String raw) throws ImapCommand.SyntaxException {
        String[] segments = raw.split("\\s+");
        if (segments.length < 1)
            throw new ImapCommand.SyntaxException("not enough arguments");
        else if (segments.length > 1)
            throw new ImapCommand.SyntaxException("too many arguments");

        if (!Globals.MAILBOX_PATTERN.matcher(segments[0]).matches())
            throw new ImapCommand.SyntaxException("invalid mailbox name");

        return new ImapMailboxArgument(segments[0]);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getMailbox() {
        return m_Mailbox;
    }
}
