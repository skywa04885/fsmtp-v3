package nl.fannst.smtp;

import java.util.Arrays;

public class SmtpServerCapability {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum Type {
        STARTTLS("STARTTLS"),
        ENHANCED_STATUS_CODES("ENHANCEDSTATUSCODES"),
        CHUNKING("CHUNKING"),
        BINARY_MIME("BINARYMIME"),
        PIPELINING("PIPELINING"),
        SMTP_UTF8("SMTPUTF8"),
        SIZE("SIZE"),
        DSN("DSN"),
        _8BIT_MIME("8BITMIME"),
        AUTH("AUTH");

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final String m_Keyword;

        /**
         * Creates new type with keyword
         * @param keyword the keyword
         */
        Type(String keyword)
        {
            m_Keyword = keyword;
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public String m_GetKeyword() {
            return m_Keyword;
        }

        /****************************************************
         * Static Methods
         ****************************************************/

        /**
         * Gets an type from string
         * @param raw the raw string
         * @return the type
         */
        public static Type fromString(String raw) {
            for (Type type : Type.values()) {
                if (type.m_GetKeyword().equalsIgnoreCase(raw)) return type;
            }

            return null;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final Type m_Type;
    private final String[] m_Arguments;

    /**
     * Creates new server capability with type and arguments
     * @param type the type
     * @param arguments the arguments
     */
    public SmtpServerCapability(Type type, String[] arguments)
    {
        m_Type = type;
        m_Arguments = arguments;
    }

    /**
     * Creates new server capability with type only
     * @param type the type
     */
    public SmtpServerCapability(Type type)
    {
        this(type, null);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Override method to create string
     * @return the string of current capability
     */
    @Override
    public String toString() {
        if (m_Arguments == null) {
            return m_Type.m_GetKeyword();
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(m_Type.m_GetKeyword());
        for (String arg : m_Arguments) {
            stringBuilder.append(' ').append(arg);
        }

        return stringBuilder.toString();
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    /**
     * Parses an raw string into server capability
     * @param raw the raw string
     * @return the server capability
     */
    public static SmtpServerCapability parse(String raw) {
        String[] segments = raw.split("\\s+");
        if (segments.length < 1) {
            return null;
        }

        Type type = Type.fromString(segments[0]);
        if (type == null) {
            return null;
        }

        return new SmtpServerCapability(type, Arrays.copyOfRange(segments, 1, segments.length));
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
