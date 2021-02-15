package nl.fannst.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Passwords {
    /**
     * Verifies an password with hash
     * @param hash the hash
     * @param password the password
     * @return success or not
     * @throws Exception possible exception
     */
    public static boolean verify(String hash, String password) throws Exception
    {
        String[] segments = hash.split("\\.");
        assert (segments.length == 3);

        // Gets the original hash, the salt and the number of iterations
        //  which have been performed on the hash
        byte[] originalHash = Base64.getDecoder().decode(segments[0]);
        byte[] salt = Base64.getDecoder().decode(segments[1]);
        int iterations = Integer.parseInt(segments[2]);

        // Generates the hash from the specified password, so we can later check if there
        //  is an match.
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 64 * 8);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] newHash = secretKeyFactory.generateSecret(spec).getEncoded();

        // Checks if the password is valid
        return Arrays.equals(newHash, originalHash);
    }
}
