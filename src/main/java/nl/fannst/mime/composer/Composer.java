package nl.fannst.mime.composer;

import nl.fannst.Globals;
import nl.fannst.mime.Address;
import nl.fannst.mime.ContentType;
import nl.fannst.mime.Header;
import nl.fannst.mime.MIMEVersion;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Composer {
    /****************************************************
     * Classy Stuff
     ****************************************************/

    private final ArrayList<ComposeSection> m_Sections;
    private ArrayList<Header> m_CustomHeaders;
    private final ArrayList<Address> m_From;
    private final ArrayList<Address> m_To;
    private String m_Subject;

    public Composer() {
        m_Sections = new ArrayList<>();
        m_From = new ArrayList<>();
        m_To = new ArrayList<>();
    }

    /****************************************************
     * Instance Methods
     ****************************************************/

    /**
     * Composes an MIME message with current config
     *
     * @return composed message
     */
    public String compose() {
        StringBuilder stringBuilder = new StringBuilder();

        String messageID = generateMessageID(),
                boundary = generateBoundary();

        //
        // Builds the headers
        //

        ArrayList<Header> headers = new ArrayList<>();
        if (m_CustomHeaders != null) headers.addAll(m_CustomHeaders);

        headers.add(new Header("X-Mailer", "Skynet Mailer, FSMTP-V3"));

        headers.add(new Header("MIME-Version", MIMEVersion.M1_0.getKeyWord()));
        headers.add(new Header("Content-Type", ContentType.MULTIPART_ALTERNATIVE.withBoundary(boundary)));

        headers.add(new Header("Date", Globals.MIME_DATE_FORMAT.format(new Date())));
        headers.add(new Header("From", Address.buildAddressList(m_From)));
        headers.add(new Header("To", Address.buildAddressList(m_To)));
        headers.add(new Header("Subject", m_Subject));

        headers.add(new Header("Message-ID", messageID));

        stringBuilder.append(Header.buildHeaders(headers)).append("\r\n");

        //
        // Builds the body
        //

        for (ComposeSection section : m_Sections) {
            stringBuilder.append("--").append(boundary).append("\r\n");
            stringBuilder.append(section.build()).append("\r\n");
        }

        stringBuilder.append("--").append(boundary).append("--");

        return stringBuilder.toString();
    }

    /****************************************************
     * Static Methods
     ****************************************************/

    private static final char[] MESSAGE_ID_DICT = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9'
    };

    /**
     * Generates an messageID
     *
     * @return the messageID
     */
    public static String generateMessageID() {
        StringBuilder stringBuilder = new StringBuilder();

        int[] randomNumbers = new Random().ints(0, MESSAGE_ID_DICT.length).limit(36).toArray();
        for (int n : randomNumbers) {
            stringBuilder.append(MESSAGE_ID_DICT[n]);
        }

        stringBuilder.append('_');
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append('@');

        try {
            stringBuilder.append(InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            stringBuilder.append("fannst.nl");
        }

        return stringBuilder.toString();
    }

    /**
     * Generates an message boundary
     *
     * @return the generated boundary
     */
    public static String generateBoundary() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("____");

        int[] randomNumbers = new Random().ints(0, MESSAGE_ID_DICT.length).limit(42).toArray();
        for (int n : randomNumbers) {
            stringBuilder.append(MESSAGE_ID_DICT[n]);
        }

        stringBuilder.append("____");

        return stringBuilder.toString();
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public void addSection(ComposeSection section) {
        m_Sections.add(section);
    }

    public void addHeader(Header header) {
        if (m_CustomHeaders == null)
            m_CustomHeaders = new ArrayList<>();

        m_CustomHeaders.add(header);
    }

    public void addFrom(Address from) {
        m_From.add(from);
    }

    public void addTo(Address to) {
        m_To.add(to);
    }

    public void setSubject(String subject) {
        m_Subject = subject;
    }
}
