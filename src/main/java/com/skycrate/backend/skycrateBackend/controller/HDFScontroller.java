package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.dto.ResponseDTO;
import com.skycrate.backend.skycrateBackend.services.EncryptionUtil;
import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
import com.skycrate.backend.skycrateBackend.utils.KeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import static com.skycrate.backend.skycrateBackend.utils.KeyUtil.getPrivateKeyForUser;
import static com.skycrate.backend.skycrateBackend.utils.KeyUtil.getPublicKeyForUser;

@RestController
@RequestMapping("/api/hdfs")
public class HDFScontroller {

    private final HDFSOperations hdfsOperations;

    @Autowired
    public HDFScontroller(HDFSOperations hdfsOperations) {
        this.hdfsOperations = hdfsOperations;
    }


    @PostMapping("/createFolder")
    public ResponseDTO createFolder(@RequestParam String hdfsPath) {
        try {
            hdfsOperations.createFolder(hdfsPath);
            return new ResponseDTO("Folder created successfully", true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO("Failed to create folder: " + e.getMessage(), false);
        }
    }

    @PostMapping("/uploadFile")
    public ResponseDTO uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String hdfsPath,
            @RequestParam String uploadedFileName,
            @RequestParam String username) {
        try {
            // Save file locally first
            String localPath = saveFileLocally(file);
            System.out.println("File saved locally at: " + localPath);

            // Upload file to HDFS
            hdfsOperations.uploadFile(localPath, hdfsPath, uploadedFileName, username);
            return new ResponseDTO("File uploaded successfully", true);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseDTO("Failed to upload file locally: " + e.getMessage(), false);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO("Failed to upload file to HDFS: " + e.getMessage(), false);
        }
    }


    private String saveFileLocally(MultipartFile file) throws IOException {
        // Create a temporary directory if it doesn't exist
        Path tmpDir = Paths.get("tmp");
        if (!Files.exists(tmpDir)) {
            Files.createDirectories(tmpDir);  // Create the directory if it doesn't exist
        }

        Path path = tmpDir.resolve(file.getOriginalFilename());

        // Copy the file to the local directory
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return path.toString(); // Return the local path for further processing
    }


    @PostMapping("/downloadFile")
    public ResponseDTO downloadFile(
            @RequestParam String hdfsPath,
            @RequestParam String localPath,
            @RequestParam String username) {
        try {
            hdfsOperations.downloadFile(hdfsPath, localPath, username);
            return new ResponseDTO("File downloaded successfully", true);
        } catch (Exception e) {
            return new ResponseDTO("Failed to download file: " + e.getMessage(), false);
        }
    }

    @DeleteMapping("/deleteFile")
    public ResponseDTO deleteFile(@RequestParam String hdfsPath) {
        try {
            hdfsOperations.deleteFile(hdfsPath);
            return new ResponseDTO("File deleted successfully", true);
        } catch (Exception e) {
            return new ResponseDTO("Failed to delete file: " + e.getMessage(), false);
        }
    }

    @DeleteMapping("/deleteFolder")
    public ResponseDTO deleteFolder(@RequestParam String hdfsPath) {
        try {
            hdfsOperations.deleteFolder(hdfsPath);
            return new ResponseDTO("Folder deleted successfully", true);
        } catch (Exception e) {
            return new ResponseDTO("Failed to delete folder: " + e.getMessage(), false);
        }
    }

    @GetMapping("/listFiles")
    public ResponseEntity<?> listFiles(@RequestParam String hdfsPath) {
        try {
            List<String> files = hdfsOperations.listFilesAndFolders(hdfsPath);
            return ResponseEntity.ok(files); // Returns the list as JSON array
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to list files: " + e.getMessage());
        }
    }
}