package nl.fannst.encryption;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class AES {
    public static byte[] getRandomNonce() {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static SecretKey deriveKey(char[] password, byte[] salt, int iterations, int keyLen) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLen * 8);
            SecretKeyFactory secretKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return secretKey.generateSecret(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates an AES key
     *
     * @return the key generated
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, SecureRandom.getInstanceStrong());
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the specified data using AES
     *
     * @param data the data
     * @param secretKey the secret key
     * @param iv the IV
     * @return encrypted data
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey, byte[] iv)
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Encrypts the specified data using AES and adds the IV.
     *
     * @param data the data
     * @param secretKey the secret key
     * @return the encrypted data + IV
     */
    public static byte[] easyEncrypt(byte[] data, SecretKey secretKey)
    {
        byte[] iv = getRandomNonce();
        byte[] encrypted = encrypt(data, secretKey, iv);
        assert encrypted != null : "Encryption is null.";

        return ByteBuffer.allocate(encrypted.length + iv.length)
                .put(iv)
                .put(encrypted)
                .array();
    }

    /**
     * Decrypts the specified data using AES
     *
     * @param cipherText the cipher text
     * @param secretKey the secret key
     * @param iv the iv
     * @return the decrypted data
     */
    public static byte[] decrypt(byte[] cipherText, SecretKey secretKey, byte[] iv)
    {
        // Decrypts
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey.getEncoded(), "AES"), new GCMParameterSpec(128, iv));
            return cipher.doFinal(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] easyDecrypt(byte[] encrypted, SecretKey secretKey)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(encrypted);

        // Gets the IV
        byte[] iv = new byte[16];
        byteBuffer.get(iv);

        // Gets the cipher text
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        // Returns the decrypted data
        return decrypt(cipherText, secretKey, iv);
    }
}
