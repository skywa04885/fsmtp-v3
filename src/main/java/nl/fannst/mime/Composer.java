package nl.fannst.mime;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class Composer {
    public static class Config {
        private final ArrayList<Address> m_From, m_To;
        private final String m_Subject;

        public Config(ArrayList<Address> from, ArrayList<Address> to, String subject) {
            m_From = from;
            m_To = to;
            m_Subject = subject;
        }

        public ArrayList<Address> getFrom() {
            return m_From;
        }

        public ArrayList<Address> getTo() {
            return m_To;
        }

        public String getSubject() {
            return m_Subject;
        }
    }

    private static final char[] MESSAGE_ID_DICT = {
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9'
    };

    /**
     * Generates an messageID
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

    public static String generateMultipart(String boundary, ArrayList<Message> sections) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Message section : sections) {
            stringBuilder.append("--" + boundary);

        }

        stringBuilder.append("--" + boundary + "--");

        return stringBuilder.toString();
    }
}
