package com.skycrate.backend.skycrateBackend.entity;

import jakarta.persistence.*;

@Entity
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filePath;

    private String username;

    @Lob
    private byte[] salt;

    @Lob
    private byte[] iv;

    // Getters and Setters
}