package nl.fannst.smtp;

import nl.fannst.mime.Address;
import nl.fannst.net.plain.PlainNIOClientArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public class SmtpCommand {
    private static final int s_MaxCommandLength = 512;
    private static final int s_MaxDataLineLength = 1024;

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

    public static class InvalidArgumentException extends Exception {
        public InvalidArgumentException(String s) {
            super(s);
        }
    }

    public static class AuthenticationRequiredException extends Exception {
        public AuthenticationRequiredException(String s) {
            super(s);
        }
    }

    public static class EXPN_Argument {
        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final String m_MailListName;

        /**
         * Creates new EXPN argument with mail list name
         * @param mailListName the name
         */
        public EXPN_Argument(String mailListName) {
            m_MailListName = mailListName;
        }

        /****************************************************
         * Static Methods
         ****************************************************/

        /**
         * Parses an raw args into EXPN argument
         * @param args the args
         * @return the parsed argument
         * @throws InvalidArgumentException possibly invalid
         */
        public static EXPN_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length < 1) {
                throw new InvalidArgumentException("Not enough arguments, specify mail list name.");
            } else if (args.length > 1) {
                throw new InvalidArgumentException("Too many arguments, specify mail list name only.");
            }

            return new EXPN_Argument(args[0]);
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public String getMailListName() {
            return m_MailListName;
        }
    }

    public static class AUTH_Argument {
        /****************************************************
         * Data Types
         ****************************************************/

        public static enum Mechanism {
            PLAIN("PLAIN");

            private final String m_Keyword;

            Mechanism(String keyword) {
                m_Keyword = keyword;
            }

            public String getKeyword() {
                return m_Keyword;
            }
        }

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final Mechanism m_Mechanism;
        private final String m_Argument;

        public AUTH_Argument(Mechanism mechanism, String argument) {
            m_Mechanism = mechanism;
            m_Argument = argument;
        }

        /****************************************************
         * Static Methods
         ****************************************************/

        /**
         * Parses an raw auth argument
         * @param args the raw arguments
         * @return the parsed argument
         * @throws InvalidArgumentException possibly invalid
         */
        public static AUTH_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length < 2) {
                throw new InvalidArgumentException("Not enough arguments.");
            } else if (args.length > 2) {
                throw new InvalidArgumentException("Too many arguments.");
            }

            Mechanism mechanism = null;
            for (Mechanism m : Mechanism.values()) {
                if (m.getKeyword().equalsIgnoreCase(args[0])) {
                    mechanism = Mechanism.PLAIN;
                }
            }

            if (mechanism == null) {
                throw new InvalidArgumentException("Invalid mechanism: '" + args[0] + "'.");
            }

            return new AUTH_Argument(mechanism, args[1]);
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public Mechanism getMechanism() {
            return m_Mechanism;
        }

        public String getArgument() {
            return m_Argument;
        }
    }

    public static class BDAT_Argument {
        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final int m_ChunkSize;
        private final boolean m_LastOfSequence;

        /**
         * Creates new BDAT argument instance
         * @param chunkSize the size of chunk
         * @param end is end ?
         */
        public BDAT_Argument(int chunkSize, boolean end) {
            m_ChunkSize = chunkSize;
            m_LastOfSequence = end;
        }

        /****************************************************
         * Static Methods
         ****************************************************/

        /**
         * Parses BDAT arguments
         * @param args the arguments
         * @return the parsed arguments
         * @throws InvalidArgumentException possible invalid format
         */
        public static BDAT_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length < 1) {
                throw new InvalidArgumentException("Not enough arguments, requires SIZE at least.");
            } else if (args.length > 2) {
                throw new InvalidArgumentException("Too many arguments.");
            }

            // Attempts to parse the size integer of the binary data segment
            int size;
            try {
                size = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Invalid size '" + args[0] + "'.");
            }

            // Checks if the BDAT specifies if the current data is the last of sequence
            //  if so, set last to true, and throw error if second argument is invalid.
            boolean last = false;
            if (args.length > 1) {
                if (args[1].toUpperCase(Locale.ROOT).equals("LAST")) {
                    last = true;
                } else {
                    throw new InvalidArgumentException("Argument '" + args[1] + "' is not valid! May be LAST.");
                }
            }

            return new BDAT_Argument(size, last);
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public int getChunkSize() {
            return m_ChunkSize;
        }

        public boolean getLastOfSequence() {
            return m_LastOfSequence;
        }
    }

    public static class VRFY_Argument {
        /****************************************************
         * Data Types
         ****************************************************/

        public static enum Type {
            Username, Address
        }

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final Address m_Address;
        private final String m_Username;
        private final Type m_Type;

        /**
         * Creates new VRFY argument
         * @param address the address
         * @param username the username
         * @param type the type
         */
        public VRFY_Argument(Address address, String username, Type type) {
            m_Address = address;
            m_Username = username;
            m_Type = type;
        }

        /**
         * Creates new VRFY argument with address only
         * @param address the address
         */
        public VRFY_Argument(Address address) {
            this(address, null, Type.Address);
        }

        /**
         * Creates new VRFY argument with username only
         * @param username the username
         */
        public VRFY_Argument(String username) {
            this(null, username, Type.Username);
        }

        /****************************************************
         * Static methods
         ****************************************************/

        /**
         * Parses raw argument into VRFY argument
         * @param args the argument
         * @return the parsed argument
         * @throws InvalidArgumentException possible invalid
         */
        public static VRFY_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length == 0) {
                throw new InvalidArgumentException("MAIL/RCPT requires an address.");
            }

            String arg = String.join(" ", args);

            if (arg.indexOf('@') != -1) {
                try {
                    return new VRFY_Argument(new Address(arg));
                } catch (Address.InvalidAddressException e) {
                    throw new InvalidArgumentException("Invalid address: " + e.getMessage());
                }
            } else if (args.length > 1) {
                throw new InvalidArgumentException("Too many arguments.");
            } else {
                return new VRFY_Argument(args[0]);
            }
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public Address getAddress() {
            return m_Address;
        }

        public String getUsername() {
            return m_Username;
        }

        public Type getType() {
            return m_Type;
        }
    }

    public static class MAIL_RCPT_Argument {
        /****************************************************
         * Data types
         ****************************************************/

        public static enum Type {
            FROM, TO
        };

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final Address m_Address;
        private final Type m_Type;

        public MAIL_RCPT_Argument(Address address, Type type) {
            m_Address = address;
            m_Type = type;
        }

        /****************************************************
         * Static methods
         ****************************************************/

        /**
         * Parses raw arguments into an MAIL/RCPT argument
         * @param args the arguments
         * @return the result
         * @throws InvalidArgumentException possible invalid args
         */
        public static MAIL_RCPT_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length == 0) {
                throw new InvalidArgumentException("MAIL/RCPT requires an address.");
            }

            String arg = String.join(" ", args);

            int pos = arg.indexOf(':');
            if (pos == -1) {
                throw new InvalidArgumentException("Invalid argument.");
            }

            Type type;
            try {
                type = Type.valueOf(arg.substring(0, pos).trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new InvalidArgumentException("Invalid argument FROM/TO required.");
            }

            try {
                return new MAIL_RCPT_Argument(new Address(arg.substring(pos + 1)), type);
            } catch (Address.InvalidAddressException e) {
                throw new InvalidArgumentException("Invalid address: " + e.getMessage());
            }
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public Address getAddress() {
            return m_Address;
        }

        public Type getType() {
            return m_Type;
        }
    }

    public static class HELO_EHLO_Argument {
        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final String m_Hostname;

        /**
         * Creates new hostname argument instance
         * @param hostname the hostname
         */
        public HELO_EHLO_Argument(String hostname) {
            m_Hostname = hostname;
        }

        /****************************************************
         * Static methods
         ****************************************************/

        /**
         * Parses the hostname argument
         * @param args the arguments
         * @return the parsed hostname argument
         * @throws InvalidArgumentException if the hostname is invalid
         */
        public static HELO_EHLO_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length == 0) {
                throw new InvalidArgumentException("Empty EHLO/HELO argument not allowed.");
            } else if (args.length > 1) {
                throw new InvalidArgumentException("EHLO/HELO argument \"" + String.join(" ", args) + "\" invalid.");
            }

            return new HELO_EHLO_Argument(args[0]);
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public String getHostname() {
            return m_Hostname;
        }
    }

    public static enum Type {
        /* SMTP */
        HELO, MAIL, RCPT, DATA, QUIT,
        NOOP, RSET, EHLO, HELP, VRFY,
        EXPN,
        /* ESMTP */
        AUTH,
        /* CHUNKING */
        BDAT,
        /* Fannst */
        XAUTHOR
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Type m_Type;
    private final String[] m_Arguments;

    /**
     * Creates command instance with custom values
     * @param type the type
     * @param arguments the arguments
     */
    public SmtpCommand(Type type, String[] arguments) {
        m_Type = type;
        m_Arguments = arguments;
    }

    /**
     * Creates a command instance based on existing one
     * @param command the command
     */
    public SmtpCommand(SmtpCommand command) {
        this(command.getType(), command.getArguments());
    }

    /**
     * Creates a new command instance based on raw string
     * @param raw the raw line
     * @throws SyntaxException if no command is found
     * @throws UnrecognizedException if the command is not recognized
     */
    public SmtpCommand(String raw) throws SyntaxException, UnrecognizedException {
        this(SmtpCommand.parse(raw));
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Override method for creating command string
     * @return the command string
     */
    @Override
    public String toString() {
        if (m_Arguments == null) {
            return m_Type.toString();
        }

        // Builds the result based on the command specified, and the
        //  arguments followed and separated by spaces.
        StringBuilder builder = new StringBuilder();
        builder.append(m_Type.toString());
        for (String arg : m_Arguments) {
            builder.append(' ').append(arg);
        }

        return builder.toString();
    }

    public void write(PlainNIOClientArgument client) throws IOException {
        String string = toString() + "\r\n";
        ByteBuffer byteBuffer = ByteBuffer.wrap(string.getBytes(StandardCharsets.US_ASCII));
        client.write(byteBuffer);
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Parses an raw SMTP Command
     * @param raw the raw line
     * @return the parsed command
     * @throws SyntaxException if no command is found
     * @throws UnrecognizedException if the command is not recognized
     */
    public static SmtpCommand parse(String raw) throws SyntaxException, UnrecognizedException {
        assert(raw != null);

        // Splits the raw string by space, and makes sure that at
        //  least the command is specified.
        String[] segments = raw.split("\\s+");
        if (segments.length < 1) {
            throw new SyntaxException("No command specified.");
        }

        // Gets the command type enum value from the string, if this fails
        //  it means it is unrecognized, and we throw an exception
        Type type;
        try {
            type = Type.valueOf(segments[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnrecognizedException("Command not recognized.");
        }

        // Returns the final command, with the other elements of the array
        //  as the arguments
        return new SmtpCommand(type, Arrays.copyOfRange(segments, 1, segments.length));
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public static int getMaxCommandLength() {
        return s_MaxCommandLength;
    }

    public static int getMaxDataLineLength() {
        return s_MaxDataLineLength;
    }

    public Type getType() {
        return m_Type;
    }

    public String[] getArguments() {
        return m_Arguments;
    }
}
