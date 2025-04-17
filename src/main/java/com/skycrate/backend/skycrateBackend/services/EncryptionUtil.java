package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class EncryptionUtil {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    // Generate RSA Key Pair (Public & Private)
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    // Encrypt data using AES (AES Key is encrypted using RSA)
//    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
//        // Step 1: Generate AES Key
//        SecretKey aesKey = generateAESKey();
//
//        // Encrypt data using AES
//        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
//        byte[] encryptedData = aesCipher.doFinal(data);
//
//        // Encrypt the AES key with RSA
//        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());
//
//        // Step 4: Combine encrypted AES key and encrypted data into one array
//        byte[] combined = new byte[4 + encryptedAesKey.length + encryptedData.length];
//
//        // First 4 bytes indicate the length of the AES encrypted key
//        combined[0] = (byte) (encryptedAesKey.length >> 24);
//        combined[1] = (byte) (encryptedAesKey.length >> 16);
//        combined[2] = (byte) (encryptedAesKey.length >> 8);
//        combined[3] = (byte) encryptedAesKey.length;
//
//        // Copy AES Key and Encrypted Data into the combined array
//        System.arraycopy(encryptedAesKey, 0, combined, 4, encryptedAesKey.length);
//        System.arraycopy(encryptedData, 0, combined, 4 + encryptedAesKey.length, encryptedData.length);
//
//        return combined;
//    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        SecretKey aesKey = RSAKeyUtil.generateAESKey(256); // Ensure 256 bits
        byte[] encryptedData = encryptDataWithAES(data, aesKey);
        byte[] encryptedAesKey = RSAKeyUtil.encryptAESKey(aesKey, publicKey);
        return combineEncryptedData(encryptedAesKey, encryptedData);
    }

    private static byte[] encryptDataWithAES(byte[] data, SecretKey aesKey) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return aesCipher.doFinal(data);
    }

    private static byte[] combineEncryptedData(byte[] encryptedAesKey, byte[] encryptedData) {
        byte[] combined = new byte[4 + encryptedAesKey.length + encryptedData.length];
        System.arraycopy(encryptedAesKey, 0, combined, 4, encryptedAesKey.length);
        System.arraycopy(encryptedData, 0, combined, 4 + encryptedAesKey.length, encryptedData.length);
        return combined;
    }


    // Decrypt data using RSA (AES Key is decrypted using RSA, then used for AES decryption)
    public static byte[] decrypt(byte[] encryptedCombined, PrivateKey privateKey) throws Exception {
        // Step 1: Extract AES Key length from the combined data
        int aesKeyLength = ((encryptedCombined[0] & 0xFF) << 24) |
                ((encryptedCombined[1] & 0xFF) << 16) |
                ((encryptedCombined[2] & 0xFF) << 8) |
                (encryptedCombined[3] & 0xFF);

        // Step 2: Extract the encrypted AES key and encrypted data
        byte[] encryptedAesKey = new byte[aesKeyLength];
        byte[] encryptedData = new byte[encryptedCombined.length - 4 - aesKeyLength];

        System.arraycopy(encryptedCombined, 4, encryptedAesKey, 0, aesKeyLength);
        System.arraycopy(encryptedCombined, 4 + aesKeyLength, encryptedData, 0, encryptedData.length);

        // Step 3: Decrypt the AES key using RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

        // Create AES key
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        // Decrypt the data using AES
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(encryptedData);
    }
}