package com.skycrate.backend.skycrateBackend.services;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

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
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        // Step 1: Generate AES Key
        SecretKey aesKey = generateAESKey();

        // Step 2: Encrypt data using AES
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = aesCipher.doFinal(data);

        // Step 3: Encrypt the AES key with RSA
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Step 4: Combine encrypted AES key and encrypted data into one array
        byte[] combined = new byte[4 + encryptedAesKey.length + encryptedData.length];

        // First 4 bytes indicate the length of the AES encrypted key
        combined[0] = (byte) (encryptedAesKey.length >> 24);
        combined[1] = (byte) (encryptedAesKey.length >> 16);
        combined[2] = (byte) (encryptedAesKey.length >> 8);
        combined[3] = (byte) encryptedAesKey.length;

        // Copy AES Key and Encrypted Data into the combined array
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
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        // Step 4: Decrypt the data using AES
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(encryptedData);
    }

    // Generate a random AES key
    private static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }
}
//
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.security.*;
//
//public class EncryptionUtil {
//    private static final String RSA_ALGORITHM = "RSA";
//    private static final String AES_ALGORITHM = "AES";
//    private static final int RSA_KEY_SIZE = 2048;
//    private static final int AES_KEY_SIZE = 256;
//
//    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
//        keyGen.initialize(RSA_KEY_SIZE);
//        return keyGen.generateKeyPair();
//    }
//
//    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
//        // Generate a random AES key
//        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
//        keyGen.init(AES_KEY_SIZE);
//        SecretKey aesKey = keyGen.generateKey();
//
//        // Encrypt the data with the AES key
//        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
//        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
//        byte[] encryptedData = aesCipher.doFinal(data);
//
//        // Encrypt the AES key with the RSA public key
//        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
//        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());
//
//        // Combine the encrypted AES key and the encrypted data
//        byte[] combined = new byte[encryptedAesKey.length + encryptedData.length + 4];
//        System.arraycopy(encryptedAesKey, 0, combined, 0, encryptedAesKey.length);
//        System.arraycopy(encryptedData, 0, combined, encryptedAesKey.length, encryptedData.length);
//        // Store the length of the encrypted AES key at the beginning
//        combined[encryptedAesKey.length + encryptedData.length] = (byte) (encryptedAesKey.length >> 24);
//        combined[encryptedAesKey.length + encryptedData.length + 1] = (byte) (encryptedAesKey.length >> 16);
//        combined[encryptedAesKey.length + encryptedData.length + 2] = (byte) (encryptedAesKey.length >> 8);
//        combined[encryptedAesKey.length + encryptedData.length + 3] = (byte) (encryptedAesKey.length);
//
//        return combined;
//    }
//
//    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
//        // Read the length of the encrypted AES key
//        int aesKeyLength = ((encryptedData[encryptedData.length - 4] & 0xFF) << 24) |
//                ((encryptedData[encryptedData.length - 3] & 0xFF) << 16) |
//                ((encryptedData[encryptedData.length - 2] & 0xFF) << 8) |
//                (encryptedData[encryptedData.length - 1] & 0xFF);
//
//        // Extract the encrypted AES key and the encrypted data
//        byte[] encryptedAesKey = new byte[aesKeyLength];
//        byte[] encryptedDataBytes = new byte[encryptedData.length - aesKeyLength - 4];
//        System.arraycopy(encryptedData, 0, encryptedAesKey, 0, aesKeyLength);
//        System.arraycopy(encryptedData, aesKeyLength, encryptedDataBytes, 0, encryptedDataBytes.length);
//
//        // Decrypt the AES key with the RSA private key
//        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
//        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
//        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
//        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);
//
//        // Decrypt the data with the AES key
//        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
//        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
//        return aesCipher.doFinal(encryptedDataBytes);
//    }
//}
