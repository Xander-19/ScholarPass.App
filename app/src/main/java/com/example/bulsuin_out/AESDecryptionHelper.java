package com.example.bulsuin_out;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESDecryptionHelper {

    // Constant secret key (must be 32 bytes for AES-256)
    private static final String SECRET_KEY = "12345678901234567890123456789012"; // 32-byte key

    public static String decrypt(String encryptedData) {
        try {
            // Split the IV and encrypted text
            String[] parts = encryptedData.split(":");
            String ivHex = parts[0]; // IV in hex
            String encryptedTextHex = parts[1]; // Encrypted data in hex

            // Convert IV and encrypted text from hex to bytes
            byte[] iv = hexStringToByteArray(ivHex);
            byte[] encryptedBytes = hexStringToByteArray(encryptedTextHex);

            // Create the cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Initialize the cipher for decryption
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Decrypt the data
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8); // Return as a string

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to convert hex string to byte array
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}