package com.skycrate.backend.skycrateBackend.dto;

import com.skycrate.backend.skycrateBackend.services.EncryptionUtil;

import java.security.KeyPair;

public class User {
    private String username;
    private KeyPair keyPair;

    public User(String username) throws Exception {
        this.username = username;
        this.keyPair = EncryptionUtil.generateKeyPair();
    }

    public String getUsername() {
        return username;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
