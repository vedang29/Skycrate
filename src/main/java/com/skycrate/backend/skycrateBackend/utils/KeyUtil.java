package com.skycrate.backend.skycrateBackend.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyUtil {
    public static PrivateKey getPrivateKeyForUser(String username) throws Exception {
        Path path = Paths.get("keys", username + "_private.key");
        byte[] bytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static PublicKey getPublicKeyForUser(String username) throws Exception {
        Path path = Paths.get("keys", username + "_public.key");
        byte[] bytes = Files.readAllBytes(path);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }
}
