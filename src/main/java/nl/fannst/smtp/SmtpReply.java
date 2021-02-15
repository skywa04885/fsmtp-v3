package nl.fannst.smtp;

import nl.fannst.net.NIOClientWrapperArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SmtpReply {
    /****************************************************
     * Data Types
     ****************************************************/

    public static class SyntaxException extends Exception {
        public SyntaxException(String s) {
            super(s);
        }
    }

    public static class EnhancedStatusCode {
        /* Classes */
        public static final EnhancedStatusCode classSuccess = new EnhancedStatusCode((byte) 2, (byte) 0, (byte) 0);
        public static final EnhancedStatusCode classPersistentTransientFailure = new EnhancedStatusCode((byte) 4, (byte) 0, (byte) 0);
        public static final EnhancedStatusCode classPermanentFailure = new EnhancedStatusCode((byte) 5, (byte) 0, (byte) 0);
        public static final EnhancedStatusCode classXFannst = new EnhancedStatusCode((byte) 6, (byte) 0, (byte) 0);
        /* Subjects */
        public static final EnhancedStatusCode subOtherOrUndefinedStatus = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 0);
        public static final EnhancedStatusCode subAddressingStatus = new EnhancedStatusCode((byte) 0, (byte) 1, (byte) 0);
        public static final EnhancedStatusCode subMailboxStatus = new EnhancedStatusCode((byte) 0, (byte) 2, (byte) 0);
        public static final EnhancedStatusCode subMailSystemStatus = new EnhancedStatusCode((byte) 0, (byte) 3, (byte) 0);
        public static final EnhancedStatusCode subNetworkRoutingStatus = new EnhancedStatusCode((byte) 0, (byte) 4, (byte) 0);
        public static final EnhancedStatusCode subMailDeliveryProtoStatus = new EnhancedStatusCode((byte) 0, (byte) 5, (byte) 0);
        public static final EnhancedStatusCode subMessageContentMediaStatus = new EnhancedStatusCode((byte) 0, (byte) 6, (byte) 0);
        public static final EnhancedStatusCode subSecurityOrPolicyStatus = new EnhancedStatusCode((byte) 0, (byte) 7, (byte) 0);
        /* Other or Undefined status */
        public static final EnhancedStatusCode detOtherUndefinedStatus = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 0);
        /* Address Status */
        public static final EnhancedStatusCode detBadDestinationMailboxAddress = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subAddressingStatus);
        public static final EnhancedStatusCode detBadDestinationSystemAddress = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subAddressingStatus);
        public static final EnhancedStatusCode detBadDestinationMailboxAddressSyntax = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subAddressingStatus);
        public static final EnhancedStatusCode detDestinationMailboxAddressAmbiguous = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subAddressingStatus);
        public static final EnhancedStatusCode detDestinationAddressValid = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subAddressingStatus);
        public static final EnhancedStatusCode detDestinationMailboxMoved = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 6).add(subAddressingStatus);
        public static final EnhancedStatusCode detBadSenderMailboxAddressSyntax = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 7).add(subAddressingStatus);
        public static final EnhancedStatusCode detBadSenderSystemAddress = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 8).add(subAddressingStatus);
        /* Mailbox Status */
        public static final EnhancedStatusCode detMailboxDisabled = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subMailboxStatus);
        public static final EnhancedStatusCode detMailboxFull = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subMailboxStatus);
        public static final EnhancedStatusCode detTooLarge = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subMailboxStatus);
        public static final EnhancedStatusCode detMailingListExpansionProblem = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 0).add(subMailboxStatus);
        /* Mail System Status */
        public static final EnhancedStatusCode detMailSystemFull = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subMailSystemStatus);
        public static final EnhancedStatusCode detSystemNotAcceptingNetworkMessages = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subMailSystemStatus);
        public static final EnhancedStatusCode detSystemNotCapableOfSelectedFeatures = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subMailSystemStatus);
        public static final EnhancedStatusCode detMessageTooBigForSystem = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subMailSystemStatus);
        public static final EnhancedStatusCode detIncorrectConfiguration = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subMailSystemStatus);
        /* Network and Routing status */
        public static final EnhancedStatusCode detNoAnswerFromHost = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detBadConnection = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detDirectoryServerFailure = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detUnableToRoute = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detMailSystemCongestion = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detRoutingLoopDetected = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 6).add(subNetworkRoutingStatus);
        public static final EnhancedStatusCode detDeliveryTimeExpired = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 7).add(subNetworkRoutingStatus);
        /* Mail Delivery Protocol Status */
        public static final EnhancedStatusCode detInvalidCommand = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subMailDeliveryProtoStatus);
        public static final EnhancedStatusCode detSyntaxError = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subMailDeliveryProtoStatus);
        public static final EnhancedStatusCode detTooManyRecipients = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subMailDeliveryProtoStatus);
        public static final EnhancedStatusCode detInvalidArguments = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subMailDeliveryProtoStatus);
        public static final EnhancedStatusCode detWrongProtocolVersion = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subMailDeliveryProtoStatus);
        /* Message Content or Message Media Status */
        public static final EnhancedStatusCode detMediaNotSupported = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subMessageContentMediaStatus);
        public static final EnhancedStatusCode detConversionRequiredAndProhibited = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subMessageContentMediaStatus);
        public static final EnhancedStatusCode detConversionRequiredButNotProhibited = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subMessageContentMediaStatus);
        public static final EnhancedStatusCode detConversionWithLossPerformed = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subMessageContentMediaStatus);
        public static final EnhancedStatusCode detConversionFailed = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subMessageContentMediaStatus);
        /* Security or Policy Status */
        public static final EnhancedStatusCode detDeliveryNotAuthorized = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 1).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detMailingListExpansionProhibited = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 2).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detSecurityConversionNotPossible = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 3).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detSecurityFeaturesNotSupported = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 4).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detCryptographicFailure = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 5).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detCryptographicAlgorithmNotSupported = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 6).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detMessageIntegrityFailure = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 7).add(subSecurityOrPolicyStatus);
        public static final EnhancedStatusCode detAuthenticationCredentialsInvalid = new EnhancedStatusCode((byte) 0, (byte) 0, (byte) 8).add(subSecurityOrPolicyStatus);

        /****************************************************
         * Data Types
         ****************************************************/

        public static class SyntaxException extends Exception {
            public SyntaxException(String s) {
                super(s);
            }
        }

        /****************************************************
         * Classy Stuff
         ****************************************************/

        private final byte m_Class;
        private final byte m_Subject;
        private final byte m_Detail;

        /**
         * Creates a new Enhanced Status Code
         * @param _class the class
         * @param subject the subject
         * @param detail the detail
         */
        public EnhancedStatusCode(byte _class, byte subject, byte detail) {
            m_Class = _class;
            m_Subject = subject;
            m_Detail = detail;
        }

        /**
         * Copies an enhanced status code
         * @param enhancedStatusCode the existing one
         */
        public EnhancedStatusCode(EnhancedStatusCode enhancedStatusCode) {
            this(enhancedStatusCode.getClass_(), enhancedStatusCode.getSubject(), enhancedStatusCode.getDetail());
        }

        /**
         * Parses an enhanced status code
         * @param raw the raw code
         * @throws SyntaxException if the code is invalid
         */
        public EnhancedStatusCode(String raw) throws SyntaxException {
            this(parse(raw));
        }

        /****************************************************
         * Instance Methods
         ****************************************************/

        /**
         * Override method to return string value of enhanced code
         * @return the enhanced code string
         */
        @Override
        public String toString() {
            return "" + m_Class + '.' + m_Subject + '.' + m_Detail;
        }

        /**
         * Adds two enhanced status codes, and returns new one
         * @param enhancedStatusCode the other one
         * @return the added ones
         */
        public EnhancedStatusCode add(EnhancedStatusCode enhancedStatusCode)
        {
            return new EnhancedStatusCode((byte) (m_Class + enhancedStatusCode.getClass_()),
                    (byte) (m_Subject + enhancedStatusCode.getSubject()),
                    (byte) (m_Detail + enhancedStatusCode.getDetail()));
        }

        /****************************************************
         * Static Methods
         ****************************************************/

        /**
         * Parses an enhanced status code from string
         * @param raw the raw code
         * @return the parsed code
         * @throws SyntaxException if the code is invalid
         */
        public static EnhancedStatusCode parse(String raw) throws SyntaxException {
            // Splits the enhanced status code by the . chars in it, if the length
            //  is not three, throw syntax exception, since it is not a valid code.
            String[] segments = raw.split("\\.");
            if (segments.length != 3) {
                throw new SyntaxException("Invalid enhanced status code.");
            }

            // Attempts to parse the class, subject and detail. If one
            //  of these fails, throw a syntax exception.
            byte _class, subject, detail;
            try {
                _class = Byte.parseByte(segments[0]);
                subject = Byte.parseByte(segments[1]);
                detail = Byte.parseByte(segments[2]);
            } catch (NumberFormatException e) {
                throw new SyntaxException("Non-digit values in enhanced status code.");
            }

            // Return the enhanced status code with the parsed class, subject and detail
            return new EnhancedStatusCode(_class, subject, detail);
        }

        /****************************************************
         * Getters / Setters
         ****************************************************/

        public byte getDetail() {
            return m_Detail;
        }

        public byte getClass_() {
            return m_Class;
        }

        public byte getSubject() {
            return m_Subject;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private static final String s_Suffix = "fsmtp";
    private static final int s_MaxLineLength = 76;

    private final int m_Code;
    private boolean m_More;
    private String m_Message;
    private final EnhancedStatusCode m_EnhancedCode;
    private final List<String> m_Lines;

    /**
     * Creates a new classic reply without message
     * @param code the code
     */
    public SmtpReply(int code) {
        this(code, null, null, false);
    }

    /**
     * Creates a new classic reply message
     * @param code the code
     * @param message the message
     */
    public SmtpReply(int code, String message) {
        this(code, message, false);
    }

    /**
     * Creates a new classic reply message
     * @param code the code
     * @param message the message
     * @param more if there are more lines
     */
    public SmtpReply(int code, String message, boolean more) {
        m_Code = code;
        m_Message = message;
        m_EnhancedCode = null;
        m_Lines = null;
        m_More = more;
    }

    /**
     * Creates an line-based reply
     * @param code the code
     * @param lines the lines
     */
    public SmtpReply(int code, List<String> lines) {
        m_Code = code;
        m_Message = null;
        m_EnhancedCode = null;
        m_Lines = lines;
        m_More = false;
    }

    /**
     * Creates a new enhanced reply
     * @param code the code
     * @param enhancedStatusCode the enhanced code
     * @param message the message
     */
    public SmtpReply(int code, EnhancedStatusCode enhancedStatusCode, String message) {
        this(code, enhancedStatusCode, message, false);
    }

    /**
     * Creates a new enhanced reply
     * @param code the code
     * @param enhancedStatusCode the enhanced code
     * @param message the message
     * @param more if there is more
     */
    public SmtpReply(int code, EnhancedStatusCode enhancedStatusCode, String message, boolean more) {
        m_Code = code;
        m_Message = message;
        m_EnhancedCode = enhancedStatusCode;
        m_Lines = null;
        m_More = more;
    }

    /**
     * Writes the current reply to socket channel
     * @param client the socket channel
     * @throws IOException possible exception
     */
    public void write(NIOClientWrapperArgument client) throws IOException {
        // Builds the reply string
        String reply = toString();
//        System.out.println("OUT -> " + reply);

        // Allocates the reply byte buffer
        ByteBuffer buffer = ByteBuffer.wrap(reply.getBytes(StandardCharsets.UTF_8));

        // Writes the byte buffer with the reply to the client
        client.write(buffer);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * The override method to create a string version of reply
     * @return the string version
     */
    @Override
    public String toString() {
        assert(m_Message != null);

        if (m_Lines != null) {
            return buildReplyLines(m_Code, m_Lines);
        }

        // Gets the length of both codes combined
        int len = "000 ".length();
        if (m_EnhancedCode != null) {
            len += "1.1.1 ".length();
        }

        // Adds the message length with the suffix after
        len += m_Message.length() + 1 + s_Suffix.length();

        // Builds the message in a wrapped style, if it is too
        //  large for a single line
        if (len > s_MaxLineLength) {
            return buildWrappedReply(m_Code, m_Message + " - " + s_Suffix);
        } else {
            return buildReplyLine(m_Code, m_EnhancedCode, m_Message + " - " + s_Suffix, true);
        }
    }

    public void parseAndAppendMessage(String raw) throws SyntaxException {
        if (raw.length() < 3) {
            throw new SyntaxException("Reply too short.");
        }

        // Attempt to parse the code, if the code is invalid throw
        //  syntax exception.
        int code;
        try {
            code = Integer.parseInt(raw.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid code.");
        }

        // If the code does not match our current one, also throw an exception.
        if (code != m_Code) {
            throw new SyntaxException("Code mismatch, " + code + ':' + m_Code);
        }

        // Appends the message;
        if (raw.length() >= 4) {
            m_Message += "\r\n" + raw.substring(4);
        }

        // Checks if the separator is an '-', if so return true
        //  since there will be more lines, else return false.
        if (raw.charAt(3) == '-') {
            m_More = true;
        } else {
            m_More = false;
        }
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    private static final Pattern s_EnhancedCodePattern = Pattern.compile("\\d.\\d.\\d");

    /**
     * Creates an wrapped message
     * @param c the code
     * @param m the message
     * @return the wrapped result
     */
    public static String buildWrappedReply(int c, String m) {
        ArrayList<String> lines = new ArrayList<>();

        StringBuilder line = new StringBuilder();
        int max = s_MaxLineLength - 4;

        for (String segment : m.split("\\s+")) {
            if (line.length() + segment.length() >= max) {
                // Checks if the segment fits in a single line, if it is too
                //  large first split it into multiple rows, else just add
                //  it immediately
                if (segment.length() > s_MaxLineLength) {

                    // Starts wrapping the long string over multiple lines
                    int s = 0;
                    while (s < segment.length()) {
                        if (segment.length() > (s + max)) {
                            lines.add(segment.substring(s, s + max).trim());
                            s += max;
                        } else {
                            int left = segment.length() - s;
                            lines.add(segment.substring(s, s + left).trim());
                            s += left;
                        }
                    }
                } else {
                    // Adds the current line to the final lines, and creates
                    //  a new string builder
                    lines.add(line.toString().trim());
                    line = new StringBuilder();
                }
            }

            // Appends the current segment with the space behind it
            line.append(segment).append(' ');
        }

        // If there is any char / chars left in the currently built line
        //  add them to the final lines
        if (line.length() > 0) {
            lines.add(line.toString().trim());
        }

        // Builds the result lines
        return buildReplyLines(c, lines);
    }

    /**
     * Builds a list of lines into smtp ones
     * @param c the code
     * @param lines the lines
     * @return the result
     */
    public static String buildReplyLines(int c, List<String> lines) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < lines.size(); ++i) {
            stringBuilder.append(buildReplyLine(c, null, lines.get(i), (i + 1) == lines.size()));
        }

        return stringBuilder.toString();
    };

    /**
     * Builds a single response line
     * @param c the code
     * @param ec the enhanced code
     * @param m the message
     * @param los last of sequence
     * @return the result
     */
    public static String buildReplyLine(int c, EnhancedStatusCode ec, String m, boolean los) {
        StringBuilder stringBuilder = new StringBuilder();

        // Adds the code, and an space if it is the last of sequence, else
        //  an dash to indicate more is coming
        stringBuilder.append(c).append(los ? ' ' : '-');

        // If there is an enhanced status code, also add it followed
        //  by a whitespace char
        if (ec != null) {
            stringBuilder.append(ec.toString()).append(' ');
        }

        // Appends the message
        stringBuilder.append(m).append("\r\n");

        return stringBuilder.toString();
    }

    /**
     * Parses an raw reply message
     * @param raw the raw reply
     * @return the parsed reply
     * @throws SyntaxException if the reply is invalid
     */
    public static SmtpReply parse(String raw) throws SyntaxException {
        assert(raw != null);

        if (raw.length() < 3) {
            throw new SyntaxException("Invalid reply.");
        }

        // Parses the integer value of the code, if this is invalid
        //  throw syntax exception
        int code;
        try {
            code = Integer.parseInt(raw.substring(0, 3));
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid code found.");
        }

        // Splits the rest of the response into segments
        String[] segments = raw.substring(3).split("\\s+");

        // If there only was a code, just return the reply
        //  with the code only
        if (segments.length == 0) {
            return new SmtpReply(code);
        }

        // Checks if the reply contains an enhanced status code, if so
        //  parse it first, and than use the rest as reply message, else
        //  just use all segments as reply message.
        if (s_EnhancedCodePattern.matcher(segments[0]).matches()) {
            // Parses the enhanced status code, and rethrows the exception
            //  if the code is invalid
            EnhancedStatusCode enhancedStatusCode;
            try {
                enhancedStatusCode = new EnhancedStatusCode(segments[0]);
            } catch (EnhancedStatusCode.SyntaxException e) {
                throw new SyntaxException("Invalid enhanced status code.");
            }

            // Returns the reply with the enhanced status code, and the
            //  joined segments as the message
            return new SmtpReply(code, enhancedStatusCode,
                    String.join(" ", Arrays.copyOfRange(segments, 1, segments.length)).trim(), raw.charAt(3) == '-');
        }

        // Returns a new reply with the code, and the rest of the segments
        //  as the message
        return new SmtpReply(code, String.join(" ", Arrays.copyOfRange(segments, 0, segments.length)).trim(), raw.charAt(3) == '-');
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public boolean getMore() {
        return m_More;
    }

    public int getCode() {
        return m_Code;
    }

    public EnhancedStatusCode getEnhancedCode() {
        return m_EnhancedCode;
    }

    public String getMessage() {
        return m_Message;
    }
}
