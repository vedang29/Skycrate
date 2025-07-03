package com.skycrate.backend.skycrateBackend.services;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final int SALT_LENGTH = 16;       // in bytes
    private static final int IV_LENGTH = 16;         // for AES CBC
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;       // bits

    // --- AES key derivation using PBKDF2 ---
    public static SecretKey deriveAESKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    // --- Encrypt data using AES-CBC ---
    public static byte[] encrypt(byte[] data, SecretKey key, byte[] iv)
            throws GeneralSecurityException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        return cipher.doFinal(data);
    }

    // --- Decrypt data using AES-CBC ---
    public static byte[] decrypt(byte[] encryptedData, SecretKey key, byte[] iv)
            throws GeneralSecurityException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        return cipher.doFinal(encryptedData);
    }

    // --- Generate random salt ---
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // --- Generate random IV ---
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // --- Optional: Utility to base64 encode data ---
    public static String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}