package com.skycrate.backend.skycrateBackend.repository;

import com.skycrate.backend.skycrateBackend.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByUsernameAndFilePath(String username, String filePath);
}