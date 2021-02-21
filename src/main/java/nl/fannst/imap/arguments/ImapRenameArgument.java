package nl.fannst.imap.arguments;

import nl.fannst.imap.ImapCommand;

import java.util.regex.Pattern;

public class ImapRenameArgument extends ImapCommandArgument {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Original;
    private final String m_New;

    public ImapRenameArgument(String original, String _new) {
        m_Original = original;
        m_New = _new;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    private static final Pattern MAILBOX_PATTERN = Pattern.compile("^([A-Za-z0-9-_\\[\\]/]+)$");
    public static ImapRenameArgument parse(String raw) throws ImapCommand.SyntaxException {
        String[] segments = raw.split("\\s+");
        if (segments.length < 2)
            throw new ImapCommand.SyntaxException("not enough arguments");
        else if (segments.length > 2)
            throw new ImapCommand.SyntaxException("too many arguments");

        if (!MAILBOX_PATTERN.matcher(segments[0]).matches())
            throw new ImapCommand.SyntaxException("invalid original mailbox name");
        if (!MAILBOX_PATTERN.matcher(segments[1]).matches())
            throw new ImapCommand.SyntaxException("invalid new mailbox name");

        return new ImapRenameArgument(segments[0], segments[1]);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public String getOriginal() {
        return m_Original;
    }

    public String getNew() {
        return m_New;
    }
}
