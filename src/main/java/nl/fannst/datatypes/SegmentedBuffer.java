package nl.fannst.datatypes;

/*
    The Magic Behind Pipelining & Async Sockets ;)
 */

public class SegmentedBuffer {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final String m_Terminator;
    private String m_Buffer;
    private int m_Pos;

    /**
     * Creates a new segmented buffer with the specified terminator
     * @param terminator
     */
    public SegmentedBuffer(String terminator)
    {
        m_Pos = 0;
        m_Buffer = "";
        m_Terminator = terminator;
    }

    /**
     * Creates a new segmented buffer with the default <CR></LF>
     */
    public SegmentedBuffer()
    {
        this("\r\n");
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Adds an string to the segmented buffer
     * @param str the string
     */
    public void add(String str) {
        m_Buffer += str;
    }

    /**
     * Shifts the already read data out of the buffer, this will be done after
     *  all lines available are processed
     */
    public void shift() {
        m_Buffer = m_Buffer.substring(m_Pos);
        m_Pos = 0;
    }

    /**
     * Checks if there are any lines available
     * @return available ?
     */
    public boolean lineAvailable() {
        return m_Buffer.indexOf(m_Terminator, m_Pos) != -1;
    }

    /**
     * Reads a single segment until terminator from buffer
     * @return the result
     */
    public String read() {
        // Checks if there is any terminator in the buffer, if not
        //  just return null.
        int pos = m_Buffer.indexOf(m_Terminator, m_Pos);
        if (pos == -1) {
            return null;
        }

        // Adds the terminator length to the position, since we're interested
        //  in returning the terminator too
        pos += m_Terminator.length();

        // Reads the line from the buffer, and updates the current position
        //  in the buffer.
        String segment = m_Buffer.substring(m_Pos, pos);
        m_Pos = pos;

        // Returns the line
        return segment;
    }

    /**
     * Gets the number of available bytes in the buffer
     * @return the available bytes
     */
    public int availableBytes() {
        return m_Buffer.length() - m_Pos;
    }

    /**
     * Reads N bytes from the buffer, returns null if not available
     * @param n the number of bytes
     * @return the bytes
     */
    public String read(int n) {
        if (availableBytes() < n) {
            return null;
        }

        String segment = m_Buffer.substring(m_Pos, m_Pos + n);
        m_Pos += n;
        return segment;
    }

    /**
     * Flushes the buffer instance
     */
    public void flush() {
        m_Buffer = "";
        m_Pos = 0;
    }
}
