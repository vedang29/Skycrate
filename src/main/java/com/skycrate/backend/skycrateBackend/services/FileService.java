package com.skycrate.backend.skycrateBackend.services;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.entity.FileMetadata;
import com.skycrate.backend.skycrateBackend.entity.User;
import com.skycrate.backend.skycrateBackend.repository.FileMetadataRepository;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import com.skycrate.backend.skycrateBackend.utils.EncryptionUtil;
import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    public FileService(FileMetadataRepository fileMetadataRepository, UserRepository userRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.userRepository = userRepository;
    }

    public void uploadEncryptedFile(String username, byte[] fileContent, String filename) throws Exception {
        log.info("Starting upload for user={}, file={}", username, filename);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            SecretKey aesKey = EncryptionUtil.generateAESKey();
            byte[] salt = EncryptionUtil.generateSalt(); // reserved for future use
            byte[] iv = EncryptionUtil.generateIv();

            byte[] encryptedData = EncryptionUtil.encrypt(fileContent, aesKey, iv);

            PublicKey publicKey = RSAKeyUtil.decodePublicKey(user.getPublicKey());
            byte[] encryptedAesKey = EncryptionUtil.encryptRSA(aesKey.getEncoded(), publicKey);

            Path userDir = new Path("/" + username);
            Path filePath = new Path(userDir, filename);
            FileSystem fs = HDFSConfig.getHDFS();

            if (!fs.exists(userDir)) {
                log.info("Creating directory in HDFS: {}", userDir);
                fs.mkdirs(userDir);
            }

            log.info("Writing encrypted file to HDFS: {}", filePath);
            try (FSDataOutputStream out = fs.create(filePath, true);
                 ByteArrayInputStream in = new ByteArrayInputStream(encryptedData)) {
                in.transferTo(out);
            }

            FileMetadata metadata = FileMetadata.builder()
                    .username(username)
                    .filePath(filePath.toString())
                    .salt(salt)
                    .iv(iv)
                    .encryptedKey(encryptedAesKey)
                    .uploadedAt(System.currentTimeMillis())
                    .build();

            fileMetadataRepository.save(metadata);
            log.info("Upload complete: file={} for user={}", filename, username);

        } catch (Exception e) {
            log.error("Error during file upload for user={}, file={}: {}", username, filename, e.getMessage(), e);
            throw e;
        }
    }

    public byte[] downloadDecryptedFile(String username, String password, String filename) throws Exception {
        log.info("Download request: user={}, file={}", username, filename);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            Path filePath = new Path("/" + username + "/" + filename);
            FileMetadata metadata = fileMetadataRepository.findByUsernameAndFilePath(username, filePath.toString())
                    .orElseThrow(() -> new RuntimeException("File metadata not found for: " + filePath));

            SecretKey derivedKey = EncryptionUtil.deriveKey(password.toCharArray(), user.getPrivateKeySalt());
            byte[] decryptedPrivateKeyBytes = EncryptionUtil.decrypt(user.getPrivateKey(), derivedKey, user.getPrivateKeyIv());
            PrivateKey privateKey = RSAKeyUtil.decodePrivateKey(decryptedPrivateKeyBytes);

            byte[] aesKeyBytes = EncryptionUtil.decryptRSA(metadata.getEncryptedKey(), privateKey);
            SecretKey aesKey = EncryptionUtil.rebuildAESKey(aesKeyBytes);

            FileSystem fs = HDFSConfig.getHDFS();
            byte[] encryptedData;
            try (FSDataInputStream in = fs.open(filePath)) {
                encryptedData = in.readAllBytes();
            }

            return EncryptionUtil.decrypt(encryptedData, aesKey, metadata.getIv());

        } catch (Exception e) {
            log.error("Download failed for user={}, file={}: {}", username, filename, e.getMessage(), e);
            throw e;
        }
    }
}