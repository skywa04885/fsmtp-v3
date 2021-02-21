package nl.fannst.imap;

import nl.fannst.imap.arguments.*;

public class ImapCommand {
    /****************************************************
     * Data Types
     ****************************************************/

    public static class SyntaxException extends Exception {
        public SyntaxException(String s) {
            super(s);
        }
    }

    public static class UnrecognizedException extends Exception {
        public UnrecognizedException(String s) {
            super(s);
        }
    }

    public static class SequenceException extends Exception {
        public SequenceException(String s) {
            super(s);
        }
    }

    public static enum Type {
        /* Client Commands - Any State */
        CAPABILITY("CAPABILITY"),
        NOOP("NOOP"),
        LOGOUT("LOGOUT"),
        /* Client Commands - Not Authenticated State */
        STARTTLS("STARTTLS"),
        AUTHENTICATE("AUTHENTICATE"),
        LOGIN("LOGIN"),
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

        public static Type fromString(String a) {
            for (Type type : Type.values()) {
                if (type.getKeyword().equalsIgnoreCase(a))
                    return type;
            }

            return null;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_SequenceNo;
    private final Type m_Type;
    private final ImapCommandArgument m_Argument;

    public ImapCommand(String sequenceNo, Type type, ImapCommandArgument argument) {
        m_SequenceNo = sequenceNo;
        m_Type = type;
        m_Argument = argument;
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

    public ImapCommandArgument getArgument() {
        return m_Argument;
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static ImapCommand parse(String raw) throws SyntaxException, UnrecognizedException {
        raw = raw.replaceAll("\\s+", " ").trim();

        // Gets the sequence number, if this fails throw syntax
        //  error since this is required.
        int snPos = raw.indexOf(' ');
        if (snPos == -1)
            throw new SyntaxException("invalid tag");

        String sequenceNumber = raw.substring(0, snPos);

        // Gets the command keyword, if there is no other WSP found, we
        //  will assume it has no args, and make the pos the end of string.
        int cmPos = raw.indexOf(' ', snPos + 1);
        if (cmPos == -1)
            cmPos = raw.length();

        String rawType = raw.substring(snPos + 1, cmPos);

        // Parses the command keyword to an command enum.
        Type type = Type.fromString(rawType);
        if (type == null)
            throw new UnrecognizedException("unknown command");

        // Parses the argument based on the specified command type, except when it is null
        //  than we just have no argument.
        String rawArgString = cmPos == raw.length() ? "" : raw.substring(cmPos + 1);
        ImapCommandArgument argument = null;

        switch (type) {
            case LOGIN -> argument = (ImapCommandArgument) ImapLoginArgument.parse(rawArgString);
            case SELECT, EXAMINE, CREATE, DELETE -> argument = (ImapCommandArgument) ImapMailboxArgument.parse(rawArgString);
            case RENAME -> argument = (ImapCommandArgument) ImapRenameArgument.parse(rawArgString);
            case STATUS -> argument = (ImapStatusArgument) ImapStatusArgument.parse(rawArgString);
        }

        // Returns the parsed command.
        return new ImapCommand(sequenceNumber, type, argument);
    }
}
