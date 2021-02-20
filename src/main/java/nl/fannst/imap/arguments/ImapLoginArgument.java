package nl.fannst.imap.arguments;

import nl.fannst.imap.ImapCommand;
import nl.fannst.mime.Address;

public class ImapLoginArgument extends ImapCommandArgument {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Address m_User;
    private final String m_Password;

    public ImapLoginArgument(Address user, String password) {
        m_User = user;
        m_Password = password;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapLoginArgument parse(String raw) throws ImapCommand.SyntaxException {
        String[] segments = raw.split("\\s+");
        if (segments.length < 2)
            throw new ImapCommand.SyntaxException("not enough arguments");
        else if (segments.length > 2)
            throw new ImapCommand.SyntaxException("too many arguments");

        try {
            return new ImapLoginArgument(Address.parse(segments[0]), segments[1]);
        } catch (Address.InvalidAddressException e) {
            throw new ImapCommand.SyntaxException("invalid address: " + e.getMessage());
        }
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public Address getUser() {
        return m_User;
    }

    public String getPassword() {
        return m_Password;
    }
}
