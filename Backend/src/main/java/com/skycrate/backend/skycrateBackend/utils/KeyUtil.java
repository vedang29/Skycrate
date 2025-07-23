package com.skycrate.backend.skycrateBackend.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtil {

//    public static void generateAndStoreKeyPair(String username) throws Exception {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(2048); // Key size
//        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//
//        // Store the public key
//        Path publicKeyPath = Paths.get("C:\\Users\\sonal\\OneDrive\\Desktop\\SkyCrate\\Skycrate\\keys", username + "_public.key");
//        Files.write(publicKeyPath, keyPair.getPublic().getEncoded());
//
//        // Store the private key
//        Path privateKeyPath = Paths.get("C:\\Users\\sonal\\OneDrive\\Desktop\\SkyCrate\\Skycrate\\keys", username + "_private.key");
//        Files.write(privateKeyPath, keyPair.getPrivate().getEncoded());
//    }
//
//    public static PublicKey getPublicKeyForUser(String username) throws Exception {
//        Path path = Paths.get("C:\\Users\\sonal\\OneDrive\\Desktop\\SkyCrate\\Skycrate\\keys", username + "_public.key");
//        byte[] bytes = Files.readAllBytes(path);
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
//        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
//    }
//
//    public static PrivateKey getPrivateKeyForUser(String username) throws Exception {
//        Path path = Paths.get("C:\\Users\\sonal\\OneDrive\\Desktop\\SkyCrate\\Skycrate\\keys", username + "_private.key");
//        byte[] bytes = Files.readAllBytes(path);
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
//        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
//    }
}