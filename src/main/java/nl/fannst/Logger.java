package nl.fannst;

import javax.security.auth.callback.Callback;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

public class Logger {
    /****************************************************
     * Data Types
     ****************************************************/

    public static enum Level {
        TRACE(ConsoleColors.GREEN_BACKGROUND), INFO(ConsoleColors.WHITE), WARN(ConsoleColors.YELLOW), ERROR(ConsoleColors.RED), FATAL(ConsoleColors.RED_BOLD);

        final String m_Color;

        Level(String color) {
            m_Color = color;
        }

        public String getColor() {
            return m_Color;
        }
    }

    /****************************************************
     * Classy Stuff
     ****************************************************/

    private static final Level s_Min = Level.TRACE;

    private final String m_Prefix;
    private Level m_Level;

    /**
     * Creates a new logger with level and prefix
     * @param prefix the prefix
     * @param level the level
     */
    public Logger(String prefix, Level level) {
        m_Prefix = prefix;
        m_Level = level;
    }

    /**
     * Creates a new logger with prefix only
     * @param prefix the prefix
     */
    public Logger(String prefix) {
        this(prefix, Level.TRACE);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    private static final DateTimeFormatter s_DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    /**
     * Logs an message to the console
     * @param message the message
     * @param level the level
     */
    public void log(String message, Level level) {
        assert(message != null);
        assert(level != null);

        // Creates the string builder
        StringBuilder builder = new StringBuilder();

        // Builds the log message, with first the current timestamp, followed by the level
        //  and the prefix, after which the message is displayed
        builder.append(s_DateTimeFormatter.format(LocalDateTime.now())).append(" : ");
        builder.append(level.getColor()).append('(').append(level.toString()).append('@').append(level).append(')').append(ConsoleColors.RESET).append(" » ");
        builder.append(message);

        // Checks to which output stream the message should be put, error or just
        //  the normal stdout
        if (level == Level.ERROR || level == Level.FATAL) {
            System.err.println(builder.toString());
        } else {
            System.out.println(builder.toString());
        }
    }

    public void logTrace(Callable<String> getMessage) {
        if (s_Min != Level.TRACE)
            return;

        try {
            log(getMessage.call());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Logs an message to the console with current levle
     * @param message the message
     */
    public void log(String message) {
        log(message, m_Level);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void setLevel(Level level) {
        m_Level = level;
    }

    public static Level getMin() {
        return s_Min;
    }

    public static boolean allowTrace() {
        return s_Min == Level.TRACE;
    }
}
