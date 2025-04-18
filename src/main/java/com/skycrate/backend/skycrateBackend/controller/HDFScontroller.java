package com.skycrate.backend.skycrateBackend.controller;

import com.skycrate.backend.skycrateBackend.config.HDFSConfig;
import com.skycrate.backend.skycrateBackend.dto.ResponseDTO;
import com.skycrate.backend.skycrateBackend.models.User;
import com.skycrate.backend.skycrateBackend.repository.UserRepository;
import com.skycrate.backend.skycrateBackend.services.EncryptionUtil;
import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
import com.skycrate.backend.skycrateBackend.utils.KeyUtil;
import com.skycrate.backend.skycrateBackend.utils.RSAKeyUtil;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.springframework.core.io.FileSystemResource; // For FileSystemResource
import org.springframework.core.io.Resource; // For Resource
import org.springframework.http.HttpHeaders; // For HttpHeaders

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File; // For java.io.File

import static com.skycrate.backend.skycrateBackend.utils.KeyUtil.getPrivateKeyForUser;
import static com.skycrate.backend.skycrateBackend.utils.KeyUtil.getPublicKeyForUser;

@RestController
@RequestMapping("/api/hdfs")
public class HDFScontroller {

    private final HDFSOperations hdfsOperations;

    @Autowired
    private UserRepository userRepository;


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
            // Retrieve the user from the database using the username
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            // Get the public key from the user entity
            byte[] publicKeyBytes = user.getPublicKey();
            PublicKey publicKey = RSAKeyUtil.getPublicKeyFromBytes(publicKeyBytes);

            // Encrypt the file content using the public key
            byte[] encryptedData = encryptFile(file, publicKey);

            // Upload the encrypted file to HDFS
            hdfsOperations.uploadFile(encryptedData, hdfsPath, uploadedFileName, username);

            return new ResponseDTO("File uploaded successfully", true);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseDTO("Failed to upload file locally: " + e.getMessage(), false);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseDTO("Failed to upload file to HDFS: " + e.getMessage(), false);
        }
    }

    // Helper method to encrypt the file content using RSA encryption
    private byte[] encryptFile(MultipartFile file, PublicKey publicKey) throws Exception {
        // Step 1: Generate a random AES key
        SecretKey aesKey = generateAESKey();

        // Step 2: Encrypt the file data using AES
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] fileData = file.getBytes();
        byte[] encryptedData = aesCipher.doFinal(fileData);

        // Step 3: Encrypt the AES key with RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Step 4: Combine the encrypted AES key and the encrypted data
        byte[] combined = new byte[4 + encryptedAesKey.length + encryptedData.length];
        combined[0] = (byte) (encryptedAesKey.length >> 24);
        combined[1] = (byte) (encryptedAesKey.length >> 16);
        combined[2] = (byte) (encryptedAesKey.length >> 8);
        combined[3] = (byte) encryptedAesKey.length;

        System.arraycopy(encryptedAesKey, 0, combined, 4, encryptedAesKey.length);
        System.arraycopy(encryptedData, 0, combined, 4 + encryptedAesKey.length, encryptedData.length);

        return combined;
    }

    // Generate a random AES key
    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Use 256 bits for AES
        return keyGen.generateKey();
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
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String hdfsEncPath,
            @RequestParam String username) {
        try {
            // Extract the file name and extension
            String encFileName = new File(hdfsEncPath).getName();
            String originalFileName = encFileName.replace(".enc", "");
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

            // Define local decrypted file path
            String localDecryptedPath = "/SkyCrate/downloaded/" + originalFileName;

            // Define HDFS paths for encrypted file
            String encFilePath = "/SkyCrate/downloaded/" + encFileName;

            FileSystem fs = HDFSConfig.getHDFS();

            // Download encrypted file from HDFS
            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(hdfsEncPath), new org.apache.hadoop.fs.Path(encFilePath));

            // Retrieve the RSA private key for the user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            PrivateKey privateKey = RSAKeyUtil.getPrivateKeyFromBytes(user.getPrivateKey());

            // Read the encrypted file content
            byte[] encryptedFileContent = Files.readAllBytes(Paths.get(encFilePath));

            // Step 1: Extract the AES key length from the combined data
            int aesKeyLength = ((encryptedFileContent[0] & 0xFF) << 24) |
                    ((encryptedFileContent[1] & 0xFF) << 16) |
                    ((encryptedFileContent[2] & 0xFF) << 8) |
                    (encryptedFileContent[3] & 0xFF);

            // Step 2: Extract the encrypted AES key and encrypted data
            byte[] encryptedAesKey = new byte[aesKeyLength];
            byte[] encryptedData = new byte[encryptedFileContent.length - 4 - aesKeyLength];

            System.arraycopy(encryptedFileContent, 4, encryptedAesKey, 0, aesKeyLength);
            System.arraycopy(encryptedFileContent, 4 + aesKeyLength, encryptedData, 0, encryptedData.length);

            // Step 3: Decrypt the AES key using RSA
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

            // Create the AES key
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            // Step 4: Decrypt the data using AES
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);

            // Decrypt the file content using the provided decrypt method
//            byte[] decryptedFileContent = RSAKeyUtil.decrypt(encryptedFileContent, privateKey);
            byte[] decryptedFileContent = aesCipher.doFinal(encryptedData);

            // Write the decrypted content to the original file
            Files.write(Paths.get(localDecryptedPath + "." + fileExtension), decryptedFileContent);


            // Log the file creation
            if (Files.exists(Paths.get(localDecryptedPath + "." + fileExtension))) {
                System.out.println("File created successfully at: " + localDecryptedPath + "." + fileExtension);
            } else {
                System.out.println("Failed to create file at: " + localDecryptedPath + "." + fileExtension);
            }

            // Create the decrypted file resource
            File decryptedFile = new File(localDecryptedPath + "." + fileExtension);
            Resource resource = new FileSystemResource(decryptedFile);

            // Return the file as a response
            return ResponseEntity.ok()
                    .contentLength(decryptedFile.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + decryptedFile.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    public void initializeKeysForUser(String username) {
        try {
            // Check if the public key file exists
            Path publicKeyPath = Paths.get("C:\\Users\\sonal\\OneDrive\\Desktop\\SkyCrate\\Skycrate\\keys", username + "_public.key");
            if (!Files.exists(publicKeyPath)) {
                // Generate and store keys if they do not exist
                KeyUtil.generateAndStoreKeyPair(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
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


    @GetMapping("/getUsernameByEmail")
    public ResponseEntity<?> getUsernameByEmail(@RequestParam String email) {
        try {
            // Fetch user from the database using the provided email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

//            // Log the retrieved user object to verify the username
//            System.out.println("Retrieved user: " + user.getFullname());

            // Return the username as the response
            return ResponseEntity.ok(user.getFullname()); // Return the username
        } catch (Exception e) {
            // Handle error if user is not found or other exceptions occur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch username: " + e.getMessage());
        }
    }

}
