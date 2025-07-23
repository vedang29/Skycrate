package com.skycrate.backend.skycrateBackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String username;

    @Lob
    @Column(nullable = false)
    private byte[] salt;

    @Lob
    @Column(nullable = false)
    private byte[] iv;

    @Lob
    @Column(nullable = false, name = "encrypted_key", columnDefinition = "LONGBLOB")
    private byte[] encryptedKey;

    @Column(nullable = false)
    private long uploadedAt;
}