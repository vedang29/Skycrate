package com.skycrate.backend.skycrateBackend.services;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class EncryptionUtil {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final int RSA_KEY_SIZE = 2048;
    private static final int AES_KEY_SIZE = 256;

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        // Generate a random AES key
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // Encrypt the data with the AES key
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = aesCipher.doFinal(data);

        // Encrypt the AES key with the RSA public key
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Combine the encrypted AES key and the encrypted data
        byte[] combined = new byte[encryptedAesKey.length + encryptedData.length + 4];
        System.arraycopy(encryptedAesKey, 0, combined, 0, encryptedAesKey.length);
        System.arraycopy(encryptedData, 0, combined, encryptedAesKey.length, encryptedData.length);
        // Store the length of the encrypted AES key at the beginning
        combined[encryptedAesKey.length + encryptedData.length] = (byte) (encryptedAesKey.length >> 24);
        combined[encryptedAesKey.length + encryptedData.length + 1] = (byte) (encryptedAesKey.length >> 16);
        combined[encryptedAesKey.length + encryptedData.length + 2] = (byte) (encryptedAesKey.length >> 8);
        combined[encryptedAesKey.length + encryptedData.length + 3] = (byte) (encryptedAesKey.length);

        return combined;
    }

    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        // Read the length of the encrypted AES key
        int aesKeyLength = ((encryptedData[encryptedData.length - 4] & 0xFF) << 24) |
                ((encryptedData[encryptedData.length - 3] & 0xFF) << 16) |
                ((encryptedData[encryptedData.length - 2] & 0xFF) << 8) |
                (encryptedData[encryptedData.length - 1] & 0xFF);

        // Extract the encrypted AES key and the encrypted data
        byte[] encryptedAesKey = new byte[aesKeyLength];
        byte[] encryptedDataBytes = new byte[encryptedData.length - aesKeyLength - 4];
        System.arraycopy(encryptedData, 0, encryptedAesKey, 0, aesKeyLength);
        System.arraycopy(encryptedData, aesKeyLength, encryptedDataBytes, 0, encryptedDataBytes.length);

        // Decrypt the AES key with the RSA private key
        Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

        // Decrypt the data with the AES key
        Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(encryptedDataBytes);
    }
}
