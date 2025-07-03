package com.skycrate.backend.skycrateBackend.utils;

import javax.crypto.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class RSAKeyUtil {

    // Generate RSA Key Pair
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048); // Key size
        return generator.generateKeyPair();
    }

    // Convert byte array to PublicKey
    public static PublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    // Convert byte array to PrivateKey
    public static PrivateKey getPrivateKeyFromBytes(byte[] privateKeyBytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    // Shorthand decode aliases
    public static PublicKey decodePublicKey(byte[] publicKeyBytes) throws Exception {
        return getPublicKeyFromBytes(publicKeyBytes);
    }

    public static PrivateKey decodePrivateKey(byte[] privateKeyBytes) throws Exception {
        return getPrivateKeyFromBytes(privateKeyBytes);
    }

    // Encrypt data using RSA (with padding)
    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Decrypt data using RSA (with padding)
    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    // AES key generation
    public static SecretKey generateAESKey(int keySize) throws NoSuchAlgorithmException {
        if (keySize != 128 && keySize != 192 && keySize != 256) {
            throw new IllegalArgumentException("Invalid AES key size. Must be 128, 192, or 256 bits.");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    public static byte[] encryptAESKey(SecretKey aesKey, PublicKey publicKey) throws Exception {
        return encrypt(aesKey.getEncoded(), publicKey);
    }

    public static SecretKey decryptAESKey(byte[] encryptedAESKey, PrivateKey privateKey, int keySize) throws Exception {
        byte[] decryptedKey = decrypt(encryptedAESKey, privateKey);
        return new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
    }
}