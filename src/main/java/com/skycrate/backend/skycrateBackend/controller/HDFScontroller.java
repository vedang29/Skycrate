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
        System.out.println("==> Received createFolder call for: " + hdfsPath);
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

//    @GetMapping("/listFiles")
//    public ResponseDTO listFiles(@RequestParam String hdfsPath) {
//        try {
//            hdfsOperations.listFilesAndFolders(hdfsPath);
//            return new ResponseDTO("Listed files successfully", true);
//        } catch (Exception e) {
//            return new ResponseDTO("Failed to list files: " + e.getMessage(), false);
//        }
//    }
}






//package com.skycrate.backend.skycrateBackend.controller;
//
//import com.skycrate.backend.skycrateBackend.dto.User;
//import com.skycrate.backend.skycrateBackend.repository.UserManager;
//import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Scanner;
//
//@Component
//public class HDFScontroller implements Runnable {
//
//    private final UserManager userManager;
//    private final HDFSOperations hdfsOperations;
//
//    @Autowired
//    public HDFScontroller(UserManager userManager, HDFSOperations hdfsOperations) {
//        this.userManager = userManager;
//        this.hdfsOperations = hdfsOperations;
//    }
//
//    @Override
//    public void run() {
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.print("Enter your username: ");
//            String username = scanner.nextLine();
//
//            System.out.print("Enter your password: ");
//            String password = scanner.nextLine();
//
//            if (!userManager.authenticate(username, password)) {
//                System.out.println("❌ Authentication failed. Exiting...");
//                return;
//            }
//
//            User user;
//            try {
//                user = userManager.getUser(username);
//            } catch (Exception e) {
//                System.out.println("❌ Error creating user: " + e.getMessage());
//                return;
//            }
//
//            while (true) {
//                System.out.println("\nChoose an option:");
//                System.out.println("1. Create a folder");
//                System.out.println("2. Upload a file (encrypted)");
//                System.out.println("3. Upload a file to a specific folder (encrypted)");
//                System.out.println("4. Download a file");
//                System.out.println("5. Delete a file");
//                System.out.println("6. Delete a folder");
//                System.out.println("7. List files and folders");
//                System.out.println("8. Exit");
//
//                int choice = scanner.nextInt();
//                scanner.nextLine(); // Consume newline
//
//                try {
//                    switch (choice) {
//                        case 1:
//                            System.out.print("Enter folder name: ");
//                            hdfsOperations.createFolder(scanner.nextLine());
//                            break;
//
//                        case 2:
//                            System.out.print("Enter file path to upload: ");
//                            String filePath = scanner.nextLine();
//                            System.out.print("Enter HDFS destination path: ");
//                            String hdfsDestinationFolder = scanner.nextLine();
//                            System.out.print("Enter the name for the uploaded file: ");
//                            String uploadedFileName = scanner.nextLine();
//                            hdfsOperations.uploadFile(filePath, hdfsDestinationFolder, uploadedFileName, user);
//                            break;
//
//                        case 3:
//                            System.out.print("Enter folder name to upload to: ");
//                            String targetFolder = scanner.nextLine();
//                            System.out.print("Enter file path to upload: ");
//                            String fileToUpload = scanner.nextLine();
//                            System.out.print("Enter the name for the uploaded file: ");
//                            String uploadedFileNameSpecific = scanner.nextLine();
//                            hdfsOperations.uploadFile(fileToUpload, targetFolder, uploadedFileNameSpecific, user);
//                            break;
//
//                        case 4:
//                            System.out.print("Enter HDFS file path to download: ");
//                            String hdfsFilePath = scanner.nextLine();
//                            System.out.print("Enter local path to save the downloaded file: ");
//                            String localDownloadPath = scanner.nextLine();
//                            hdfsOperations.downloadFile(hdfsFilePath, localDownloadPath, user);
//                            break;
//
//                        case 5:
//                            System.out.print("Enter HDFS file path to delete: ");
//                            hdfsOperations.deleteFile(scanner.nextLine());
//                            break;
//
//                        case 6:
//                            System.out.print("Enter HDFS folder path to delete: ");
//                            hdfsOperations.deleteFolder(scanner.nextLine());
//                            break;
//
//                        case 7:
//                            System.out.print("Enter HDFS path to list files and folders: ");
//                            hdfsOperations.listFilesAndFolders(scanner.nextLine());
//                            break;
//
//                        case 8:
//                            System.out.println("✅ Exiting...");
//                            return;
//
//                        default:
//                            System.out.println("⚠️ Invalid option. Please try again.");
//                    }
//                } catch (Exception e) {
//                    System.out.println("❌ Operation failed: " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}























//package com.skycrate.backend.skycrateBackend.controller;
//
//import com.skycrate.backend.skycrateBackend.dto.User;
//import com.skycrate.backend.skycrateBackend.repository.UserManager;
//import com.skycrate.backend.skycrateBackend.services.HDFSOperations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Scanner;
//
//@Component
//public class HDFScontroller implements Runnable {
//
//    private final UserManager userManager;
//    private final HDFSOperations hdfsOperations;
//
//    @Autowired
//    public HDFScontroller(UserManager userManager, HDFSOperations hdfsOperations) {
//        this.userManager = userManager;
//        this.hdfsOperations = hdfsOperations;
//    }
//
//    @Override
//    public void run() {
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.print("Enter your username: ");
//            String username = scanner.nextLine();
//
//            System.out.print("Enter your password: ");
//            String password = scanner.nextLine();
//
//            if (!userManager.authenticate(username, password)) {
//                System.out.println("X Authentication failed. Exiting...");
//                return;
//            }
//
//            User user;
//            try {
//                user = userManager.getUser(username);
//            } catch (Exception e) {
//                System.out.println("X Error creating user: " + e.getMessage());
//                return;
//            }
//
//            while (true) {
//                System.out.println("\nChoose an option:");
//                System.out.println("1. Create a folder");
//                System.out.println("2. Upload a file (encrypted)");
//                System.out.println("3. Upload a file to a specific folder (encrypted)");
//                System.out.println("4. Download a file");
//                System.out.println("5. Delete a file");
//                System.out.println("6. Delete a folder");
//                System.out.println("7. List files and folders");
//                System.out.println("8. Exit");
//
//                int choice = scanner.nextInt();
//                scanner.nextLine(); // Consume newline
//
//                switch (choice) {
//                    case 1:
//                        System.out.print("Enter folder name: ");
//                        hdfsOperations.createFolder(scanner.nextLine());
//                        break;
//                    case 2:
//                        System.out.print("Enter file path to upload: ");
//                        String filePath = scanner.nextLine();
//                        System.out.print("Enter HDFS destination path: ");
//                        String hdfsDestinationFolder = scanner.nextLine();
//                        System.out.print("Enter the name for the uploaded file: ");
//                        String uploadedFileName = scanner.nextLine();
//                        hdfsOperations.uploadFile(filePath, hdfsDestinationFolder, uploadedFileName, user);
//                        break;
//                    case 3:
//                        System.out.print("Enter folder name to upload to: ");
//                        String targetFolder = scanner.nextLine();
//                        System.out.print("Enter file path to upload: ");
//                        String fileToUpload = scanner.nextLine();
//                        System.out.print("Enter the name for the uploaded file: ");
//                        String uploadedFileNameSpecific = scanner.nextLine();
//                        hdfsOperations.uploadFile(fileToUpload, targetFolder, uploadedFileNameSpecific, user);
//                        break;
//                    case 4:
//                        System.out.print("Enter HDFS file path to download: ");
//                        String hdfsFilePath = scanner.nextLine();
//                        System.out.print("Enter local path to save the downloaded file: ");
//                        String localDownloadPath = scanner.nextLine();
//                        hdfsOperations.downloadFile(hdfsFilePath, localDownloadPath, user);
//                        break;
//                    case 5:
//                        System.out.print("Enter HDFS file path to delete: ");
//                        hdfsOperations.deleteFile(scanner.nextLine());
//                        break;
//                    case 6:
//                        System.out.print("Enter HDFS folder path to delete: ");
//                        hdfsOperations.deleteFolder(scanner.nextLine());
//                        break;
//                    case 7:
//                        System.out.print("Enter HDFS path to list files and folders: ");
//                        hdfsOperations.listFilesAndFolders(scanner.nextLine());
//                        break;
//                    case 8:
//                        System.out.println("Exiting...");
//                        return;
//                    default:
//                        System.out.println("Invalid option. Please try again.");
//                }
//            }
//        }
//    }
//}
