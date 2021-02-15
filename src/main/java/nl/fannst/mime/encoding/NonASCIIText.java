package nl.fannst.mime.encoding;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NonASCIIText {
    /****************************************************
     * Data Types
     ****************************************************/

    public static class InvalidEncodedWord extends Exception {
        public InvalidEncodedWord(String m) {
            super(m);
        }
    }

    private static final Pattern s_Regex = Pattern.compile("(=\\?).*.(\\?).*.(\\?).*.(\\?)");

    public static String decodeEncodedWord(String encoded) throws InvalidEncodedWord, UnsupportedEncodingException {
        String[] segments = encoded.split("\\?");

        System.out.println(encoded);

        if (segments.length < 3) {
            throw new InvalidEncodedWord("Not enough segments.");
        } else if (segments.length > 3) {
            throw new InvalidEncodedWord("Too many segments.");
        }

        String charset = segments[0].toLowerCase(Locale.ROOT);
        String encoding = segments[1];
        if (Character.toUpperCase(encoding.charAt(0)) == 'Q') {
            byte[] decodedLine = QuotedPrintable.decodeLine(segments[2].replaceAll("_", " ").getBytes());
            return new String(decodedLine, charset);
        } else if (Character.toUpperCase(encoding.charAt(0)) == 'B') {
            return new String(Base64.getDecoder().decode(segments[2].getBytes()), charset);
        } else {
            throw new InvalidEncodedWord("Invalid encoding specified: " + Character.toUpperCase(encoding.charAt(0)));
        }
    }

    public static String decode(String raw) {
        Function<Matcher, String> replace = m -> {
            try {
                return decodeEncodedWord(m.group().substring(2, m.group().length() - 1));
            } catch (Exception e) {
                e.printStackTrace();
                return m.group();
            }
        };

        Matcher matcher = s_Regex.matcher(raw);
        if (matcher.matches()) {
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                matcher.appendReplacement(result, replace.apply(matcher));
            }

            return result.toString();
        } else {
            return raw;
        }
    }
}
