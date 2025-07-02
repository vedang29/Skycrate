package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.entity.FileMetadata;
import com.skycrate.backend.skycrateBackend.repository.FileMetadataRepository;
import com.skycrate.backend.skycrateBackend.security.EncryptionService;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileSystem hdfs;

    @Autowired
    private FileMetadataRepository metadataRepo;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam("password") String password,
                         Authentication auth) throws Exception {

        byte[] fileBytes = file.getBytes();
        byte[] salt = EncryptionService.generateSalt();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        SecretKey key = EncryptionService.deriveKey(password, salt);

        byte[] encrypted = EncryptionService.encrypt(fileBytes, key, iv);
        String pathStr = "/user/" + auth.getName() + "/" + file.getOriginalFilename();
        Path hdfsPath = new Path(pathStr);

        try (FSDataOutputStream out = hdfs.create(hdfsPath, true)) {
            out.write(encrypted);
        }

        FileMetadata metadata = new FileMetadata();
        metadata.setUsername(auth.getName());
        metadata.setFilePath(pathStr);
        metadata.setSalt(salt);
        metadata.setIv(iv);
        metadataRepo.save(metadata);

        return "File uploaded and encrypted successfully!";
    }

    @GetMapping("/download")
    public void download(@RequestParam("path") String path,
                         @RequestParam("password") String password,
                         Authentication auth,
                         OutputStream responseStream) throws Exception {

        Optional<FileMetadata> optional = metadataRepo.findByFilePathAndUsername(path, auth.getName());
        if (optional.isEmpty()) {
            throw new SecurityException("You are not authorized to access this file.");
        }

        FileMetadata metadata = optional.get();
        SecretKey key = EncryptionService.deriveKey(password, metadata.getSalt());

        try (FSDataInputStream input = hdfs.open(new Path(path))) {
            byte[] encrypted = input.readAllBytes();
            byte[] decrypted = EncryptionService.decrypt(encrypted, key, metadata.getIv());
            responseStream.write(decrypted);
        }
    }
}