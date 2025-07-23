package com.skycrate.backend.skycrateBackend.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;
import java.security.*;
import javax.crypto.spec.PBEKeySpec;
import java.util.Arrays;

public class EncryptionUtil {

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final SecureRandom secureRandom = new SecureRandom();

    // ------------------------ AES ------------------------

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static SecretKey generateAESKey() throws Exception {
        byte[] keyBytes = new byte[KEY_LENGTH / 8]; // 256 bits
        secureRandom.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static SecretKey rebuildAESKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(ciphertext);
    }

    // ------------------------ RSA ------------------------

    public static byte[] encryptRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptRSA(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

//    // --------- Encrypt/decrypt RSA private key using AES derived from password ---------
//
//    public static byte[] encryptPrivateKey(PrivateKey privateKey, String password, byte[] salt, byte[] iv) throws Exception {
//        SecretKey aesKey = deriveKey(password.toCharArray(), salt);
//        return encrypt(privateKey.getEncoded(), aesKey, iv);
//    }
//
//    public static byte[] decryptPrivateKey(byte[] encryptedPrivateKey, String password, byte[] salt, byte[] iv) throws Exception {
//        SecretKey aesKey = deriveKey(password.toCharArray(), salt);
//        return decrypt(encryptedPrivateKey, aesKey, iv);
//    }
}