package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.services.FileService;
import com.skycrate.backend.skycrateBackend.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;

    public FileController(FileService fileService, JwtService jwtService) {
        this.fileService = fileService;
        this.jwtService = jwtService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            HttpServletRequest request
    ) {
        try {
            String token = extractToken(request);
            String username = jwtService.extractUsername(token);

            fileService.uploadEncryptedFile(username, password, file.getBytes(), file.getOriginalFilename());

            return ResponseEntity.ok("File uploaded and encrypted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<?> downloadFile(
            @PathVariable String filename,
            @RequestParam("password") String password,
            HttpServletRequest request
    ) {
        try {
            String token = extractToken(request);
            String username = jwtService.extractUsername(token);

            byte[] decryptedData = fileService.downloadDecryptedFile(username, password, filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(decryptedData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File download failed: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }
}