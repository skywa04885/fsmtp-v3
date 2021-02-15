package nl.fannst.pop3;

import nl.fannst.mime.Address;

import java.util.Arrays;
import java.util.Locale;

public class PopCommand {
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

    public static enum Type {
        /* POP3 */
        QUIT, CAPA, NOOP, RSET, USER, PASS, STAT, LIST, RETR, UIDL, TOP, LAST, DELE
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Type m_Type;
    private final String[] m_Arguments;

    public PopCommand(Type type, String[] arguments) {
        m_Type = type;
        m_Arguments = arguments;
    }

    /****************************************************
     * Arguments
     ****************************************************/

    public static class TOP_Argument {
        private final int m_Index;
        private final int m_BodyLine;

        public TOP_Argument(int index, int bodyLine) {
            m_Index = index;
            m_BodyLine = bodyLine;
        }

        public static TOP_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length > 2) {
                throw new InvalidArgumentException("Too many arguments.");
            } else if (args.length < 2) {
                throw new InvalidArgumentException("Specify INDEX and LINES.");
            }

            int index;
            try {
                index = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Invalid index.");
            }

            if (index < 1) {
                throw new InvalidArgumentException("Invalid index.");
            }

            try {
                return new TOP_Argument(index, Integer.parseInt(args[1]));
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Invalid number of lines.");
            }
        }

        public int getIndex() {
            return m_Index;
        }

        public int getBodyLine() {
            return m_BodyLine;
        }
    }

    public static class RETR_DELE_Argument {
        private final int m_Index;

        public RETR_DELE_Argument(int index) {
            m_Index = index;
        }

        public static RETR_DELE_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length > 1) {
                throw new InvalidArgumentException("Too many arguments.");
            } else if (args.length < 1) {
                throw new InvalidArgumentException("Supply index.");
            }

            int index;
            try {
                index = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Invalid index.");
            }

            if (index < 1) {
                throw new InvalidArgumentException("Invalid index.");
            }

            return new RETR_DELE_Argument(index);
        }

        public int getIndex() {
            return m_Index;
        }
    }

    public static class PASS_Argument {
        private final String m_Password;

        public PASS_Argument(String password) {
            m_Password = password;
        }

        /**
         * Parses raw args into pass argument
         * @param args the raw args
         * @return the parsed arguments
         * @throws InvalidArgumentException possibly invalid
         */
        public static PASS_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length > 1) {
                throw new InvalidArgumentException("Too many arguments.");
            } else if (args.length < 1) {
                throw new InvalidArgumentException("Supply password.");
            }

            return new PASS_Argument(args[0]);
        }

        public String getPassword() {
            return m_Password;
        }
    }

    public static class USER_Argument {
        private final Address m_Address;

        public USER_Argument(Address address) {
            m_Address = address;
        }

        /**
         * Parses raw args into user argument
         * @param args the raw args
         * @return the parsed arguments
         * @throws InvalidArgumentException possibly invalid
         */
        public static USER_Argument parse(String[] args) throws InvalidArgumentException {
            if (args.length > 1) {
                throw new InvalidArgumentException("Too many arguments.");
            } else if (args.length < 1) {
                throw new InvalidArgumentException("Supply user.");
            }

            try {
                return new USER_Argument(new Address(args[0].toLowerCase(Locale.ROOT)));
            } catch (Address.InvalidAddressException e) {
                throw new InvalidArgumentException("Invalid address: " + e.getMessage());
            }
        }

        public Address getAddress() {
            return m_Address;
        }
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static PopCommand parse(String raw) throws UnrecognizedException {
        // Splits the raw into segments, and checks if there is at least one.
        String[] segments = raw.split("\\s+");
        if (segments.length < 1) {
            throw new UnrecognizedException("No command found.");
        }

        // Attempts to parse the command type, if this fails
        //  throw not recognized error.
        Type type;
        try {
            type = Type.valueOf(segments[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new UnrecognizedException("Command '" + segments[0].toUpperCase(Locale.ROOT) + "' not recognized.");
        }

        // Returns the parsed command
        return new PopCommand(type, Arrays.copyOfRange(segments, 1, segments.length));
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public Type getType() {
        return m_Type;
    }

    public String[] getArguments() {
        return m_Arguments;
    }
}
