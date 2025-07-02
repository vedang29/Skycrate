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

    public void setUsername(String username) { this.username = username; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setSalt(byte[] salt) { this.salt = salt; }
    public void setIv(byte[] iv) { this.iv = iv; }

    public String getUsername() { return this.username; }
    public String getFilePath() { return this.filePath; }
    public byte[] getSalt() { return this.salt; }
    public byte[] getIv() { return this.iv; }

}