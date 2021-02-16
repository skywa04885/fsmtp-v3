package nl.fannst.mime;

import nl.fannst.Logger;
import nl.fannst.mime.encoding.QuotedPrintable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class Message {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private ArrayList<Header> m_Headers;
    private String m_Body;

    public Message(ArrayList<Header> headers, String body) {
        m_Headers = headers;
        m_Body = body;
    }

    public Message() {
        this(new ArrayList<Header>(), null);
    }

    public Message(String body) {
        this(null, body);
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Logs the message
     * @param logger the logger to log with
     */
    public void log(Logger logger) {
        // Logs the headers
        logger.log("[Headers]:");
        for (Header header : m_Headers) {
            logger.log("\t\t" + '\'' + header.getKey() + "': '" + header.getValue() + '\'');
        }

        // Logs the message body
        logger.log('\'' + m_Body + '\'');
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    public static Message parse(String raw) throws Header.InvalidHeaderException {
        Message message = new Message();
        Scanner scanner = new Scanner(raw);

        // Parses the headers
        message.getHeaders().addAll(Header.parseHeaders(scanner));

        // Gets the body, and stores it as an string in the final message.
        StringBuilder body = new StringBuilder();
        while (scanner.hasNextLine()) {
            body.append(scanner.nextLine()).append("\r\n");
        }
        message.setBody(body.toString());

        // Returns the message
        return message;
    }

    public static ArrayList<Message> parseMultipart(String raw, String boundary) {
        ArrayList<Message> result = new ArrayList<Message>();

        Scanner scanner = new Scanner(raw);

        StringBuilder temp = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // Checks if we've hit a end boundary, or just a normal boundary
            //  so we can parse the previously read body.
            if (line.startsWith("--")) {
                boolean end = false;
                if (line.endsWith("--")) {
                    end = true;
                }

                // Checks if this actually is an boundary, if it is not just
                //  continue, else process the current section.
                if (!line.substring(2, end ? boundary.length() - 3 : boundary.length() - 1).equals(boundary)) {
                    continue;
                }

                System.out.println("asd");
            }

            // Appends the current line to the temp buffer.
            temp.append(line).append("\r\n");
        }

        return result;
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public ArrayList<Header> getHeaders() {
        return m_Headers;
    }

    public String getBody() {
        return m_Body;
    }

    public void setBody(String body) {
        m_Body = body;
    }
}
