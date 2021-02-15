package nl.fannst.mime.encoding;

import java.io.*;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class QuotedPrintable {
    private static final int s_MaxLineLength = (76 - 2);

    private static final char[] s_HexLookupTable = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Encodes an specified char to hex
     * @param c the char
     * @return the encoded char
     */
    public static String encodeChar(char c) {
        return "=" + s_HexLookupTable[c >> 4] + s_HexLookupTable[c & 0x0F];
    }

    /**
     * Performs an reverse lookup of char
     * @param c the byte
     * @return the reversed value
     */
    public static byte reverseLookupChar(byte c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else {
            return (byte) ((c - 'A') + 10);
        }
    }

    /**
     * Decodes an single quoted-printable line
     * @param raw the encoded line
     * @return the decoded line
     */
    public static byte[] decodeLine(byte[] raw) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (int i = 0; i < raw.length; ++i)
        {
            int b = raw[i];
            if (b == '=') {
                int u = reverseLookupChar(raw[++i]);
                int l = reverseLookupChar(raw[++i]);

                buffer.write((byte) (u << 4) | l);
            } else {
                buffer.write(b);
            }
        }

        return buffer.toByteArray();
    }

    /**
     * Decode an encoded message
     * @param raw the raw message
     * @param charset the charset to decode with
     * @return the decoded message
     */
    public static String decode(String raw, String charset) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        Scanner scanner = new Scanner(raw);

        StringBuilder joinedLine = new StringBuilder();
        while (scanner.hasNextLine()) {
            // Gets the current line, and appends it to the joined line.
            String line = scanner.nextLine();
            joinedLine.append(line);

            // Checks if the current line ends with an '=', if so just continue
            //  since we also need to add the next line to the joined one.
            if (line.charAt(line.length() - 1) == '=') {
                joinedLine.deleteCharAt(joinedLine.length() - 1);
                continue;
            }

            // Starts decoding the chars in the line
            byte[] decodedLine = decodeLine(joinedLine.toString().getBytes());
            result.append(new String(decodedLine, charset)).append("\r\n");
            joinedLine = new StringBuilder();
        }

        return result.toString();
    }

    /**
     * Encodes an raw body
     * @param raw the raw body
     * @return the encoded body
     */
    public static String encode(String raw) {
        StringBuilder result = new StringBuilder();

        Scanner scanner = new Scanner(raw);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int lineLength = 0;

            // Encoded the chars, but we'll skip the tabs / whitespace for now. We will
            //  handle them later.
            StringBuilder temp = new StringBuilder();
            for (char c : line.toCharArray()) {
                // All decimal values between 33 and 126 may be represented by themselves
                //  except 61 '='.
                if (((c > 33 && c < 126) && c != 61) || c == 9 || c == 32) {
                    if ((lineLength + 1) > s_MaxLineLength) {
                        temp.append("=\r\n");
                        lineLength = 1;
                    }

                    temp.append(c);
                    ++lineLength;
                } else {
                    if ((lineLength + 3) > s_MaxLineLength) {
                        temp.append("=\r\n");
                        lineLength = 1;
                    }

                    temp.append(encodeChar(c));
                    lineLength += 3;
                }
            }

            // Replaces all the tabs / whitespace at the end of the current line
            //  with encoded values, this is to prevent usage of soft line-breaks.
            char finalChar = temp.charAt(temp.length() - 1);
            if (finalChar == 9 || finalChar == 32) {
                temp.deleteCharAt(temp.length() - 1);
                --lineLength;

                if ((lineLength + 3) > s_MaxLineLength) {
                    temp.append("=\r\n");
                    lineLength = 1;
                }

                temp.append(encodeChar(finalChar));
            }

            result.append(temp.toString()).append("\r\n");
        }

        return result.toString();
    }
}
