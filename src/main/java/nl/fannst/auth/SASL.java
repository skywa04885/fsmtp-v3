package nl.fannst.auth;

import nl.fannst.datatypes.Pair;

import java.util.Base64;

public class SASL {
    public static class SASLException extends Exception {
        public SASLException(String s) {
            super(s);
        }
    }

    public static Pair<String, String> parsePlainBase64(String raw) throws SASLException {
        byte[] decodedBytes = Base64.getDecoder().decode(raw);
        String[] decoded = new String(decodedBytes).trim().split("\0");

        // Gets the username and password from the decoded Base64 string, if there
        //  are more or less than 2 segments, throw error
        if (decoded.length != 2) {
            throw new SASLException("Base64 format error.");
        }

        // Returns the username and password
        return new Pair<String, String>(decoded[0], decoded[1]);
    }
}
