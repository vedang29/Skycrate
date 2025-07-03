package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.entity.FileMetadata;
import com.skycrate.backend.skycrateBackend.repository.FileMetadataRepository;
import com.skycrate.backend.skycrateBackend.utils.EncryptionUtil;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;

    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public void uploadEncryptedFile(String username, String password, byte[] fileContent, String filename) throws Exception {
        // Generate salt and IV
        byte[] salt = EncryptionUtil.generateSalt();
        byte[] iv = EncryptionUtil.generateIv();

        // Derive AES key
        SecretKey key = EncryptionUtil.deriveKey(password.toCharArray(), salt);

        // Encrypt file content
        byte[] encryptedData = EncryptionUtil.encrypt(fileContent, key, iv);

        // Prepare HDFS path
        Path userDir = new Path("/" + username);
        Path filePath = new Path(userDir, filename);
        FileSystem fs = HDFSConfig.getHDFS();

        // Ensure user directory exists
        if (!fs.exists(userDir)) {
            fs.mkdirs(userDir);
        }

        // Write encrypted file to HDFS
        try (FSDataOutputStream outputStream = fs.create(filePath, true);
             InputStream in = new ByteArrayInputStream(encryptedData)) {
            in.transferTo(outputStream);
        }

        // Save metadata
        FileMetadata metadata = FileMetadata.builder()
                .username(username)
                .filePath(filePath.toString())
                .salt(salt)
                .iv(iv)
                .build();

        fileMetadataRepository.save(metadata);
    }

    public byte[] downloadDecryptedFile(String username, String password, String filename) throws Exception {
        Path filePath = new Path("/" + username + "/" + filename);
        FileSystem fs = HDFSConfig.getHDFS();

        Optional<FileMetadata> metadataOpt = fileMetadataRepository.findByUsernameAndFilePath(username, filePath.toString());
        if (metadataOpt.isEmpty()) {
            throw new RuntimeException("File metadata not found");
        }

        FileMetadata metadata = metadataOpt.get();

        // Derive key
        SecretKey key = EncryptionUtil.deriveKey(password.toCharArray(), metadata.getSalt());

        // Read file from HDFS
        byte[] encryptedData = Files.readAllBytes(
                new java.io.File(filePath.toString()).toPath()
        );

        // Decrypt
        return EncryptionUtil.decrypt(encryptedData, key, metadata.getIv());
    }
}